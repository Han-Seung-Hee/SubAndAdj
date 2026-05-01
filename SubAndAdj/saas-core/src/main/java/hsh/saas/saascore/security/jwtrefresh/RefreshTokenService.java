package hsh.saas.saascore.security.jwtrefresh;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Refresh 토큰 회전 정책의 핵심 서비스.
 *
 * <p>이 클래스는 다음 3가지를 보장한다.
 * <ol>
 *   <li>refresh 토큰 1회성 소비(한 번 쓰면 상태 변경)</li>
 *   <li>정상 회전 시 old -> ROTATED, new -> ACTIVE</li>
 *   <li>재사용(replay) 감지 시 family 단위 ACTIVE 토큰 폐기</li>
 * </ol>
 *
 * <p>주의: 이 클래스는 JWT 라이브러리를 직접 다루지 않는다.
 * JWT 생성/파싱은 {@link TokenProviderPort} 구현체(현재 saas-api 어댑터)로 위임한다.
 */
@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    /** JWT 생성/파싱 포트(인프라 어댑터 경계). */
    private final TokenProviderPort tokenProviderPort;

    /** refresh 토큰 상태 저장소(JPA). */
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(TokenProviderPort tokenProviderPort,
                               RefreshTokenRepository refreshTokenRepository) {
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 로그인 직후 최초 access/refresh 토큰을 발급한다.
     *
     * <p>초기 발급에서는 새 familyId를 만들고 refresh 상태를 ACTIVE로 저장한다.
     * 이후 클라이언트는 access로 보호 API를 호출하고, access 만료 시 refresh를 사용한다.
     * 이 메서드는 회전 체인의 시작점을 만드는 역할을 한다.
     */
    @Override
    @Transactional
    public RotatedTokens issueInitialTokens(Long userId, Long tenantId, String email, List<String> roles) {
        // 로그인 세션 계보(family)와 최초 refresh 식별자(jti)를 생성한다.
        String familyId = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();

        // 서명된 refresh를 만든 뒤, 실제 만료시각(exp)을 파싱해 DB 기준값으로 저장한다.
        String refreshToken = tokenProviderPort.generateRefreshToken(userId, tenantId, email, roles, familyId, jti);
        Instant exp = tokenProviderPort.parseRefreshToken(refreshToken).expiresAt();

        // 최초 refresh 상태를 ACTIVE로 영속화해 회전/재사용 탐지의 기준점으로 삼는다.
        refreshTokenRepository.save(new RefreshTokenEntity(
                jti,
                userId,
                tenantId,
                familyId,
                RefreshTokenStatus.ACTIVE,
                exp,
                Instant.now()
        ));

        // Access 토큰은 서버 저장 없이 단기 인증 용도로만 발급한다.
        String accessToken = tokenProviderPort.generateAccessToken(userId, tenantId, email, roles);
        return new RotatedTokens(accessToken, refreshToken);
    }

    /**
     * refresh 토큰을 1회 소비해 access/refresh를 재발급한다.
     *
     * <p>동시성 핵심:
     * `findByJtiForUpdate`가 행 락을 잡아 동일 refresh 토큰 동시 제출 시 1건만 성공한다.
     *
     * <p>처리 단계:
     * <ol>
     *   <li>토큰 파싱 및 jti 조회</li>
     *   <li>상태/만료 검증</li>
     *   <li>정상 시 old=ROTATED, new=ACTIVE 전이</li>
     *   <li>비정상 시 family ACTIVE 전량 REVOKED</li>
     * </ol>
     */
    @Override
    @Transactional
    public RotatedTokens rotate(String presentedRefreshToken) {
        // 1) 제출 토큰을 파싱해 클레임(jti, familyId, 사용자 컨텍스트)을 복원한다.
        ParsedRefreshToken parsed = tokenProviderPort.parseRefreshToken(presentedRefreshToken);

        // 2) 동일 jti 행에 배타 락을 걸어 동시 제출 경쟁 조건을 제거한다.
        RefreshTokenEntity current = refreshTokenRepository.findByJtiForUpdate(parsed.jti())
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh token"));

        Instant now = Instant.now();

        // ACTIVE가 아니거나 만료면 재사용/비정상으로 간주하고 family 단위로 차단한다.
        if (current.getStatus() != RefreshTokenStatus.ACTIVE || current.isExpired(now)) {
            // 현재 family의 살아있는 토큰을 일괄 폐기해 연쇄 탈취를 차단한다.
            refreshTokenRepository.revokeActiveByFamily(current.getFamilyId(), RefreshTokenStatus.REVOKED, now);
            // 문제 토큰은 REUSED로 표기해 사후 분석 시 replay 이벤트를 구분한다.
            current.markReused(now);
            throw new IllegalStateException("Refresh token replay detected");
        }

        // 3) 정상 회전: 새 refresh를 발급하고 만료시각을 계산한다.
        String nextJti = UUID.randomUUID().toString();
        String nextRefresh = tokenProviderPort.generateRefreshToken(
                parsed.userId(),
                parsed.tenantId(),
                parsed.email(),
                parsed.roles(),
                parsed.familyId(),
                nextJti
        );
        Instant nextExp = tokenProviderPort.parseRefreshToken(nextRefresh).expiresAt();

        // 기존 토큰은 회전 완료 상태로 표시한다.
        current.markRotated(nextJti, now);

        // 새 토큰을 ACTIVE로 저장해 체인을 이어간다.
        refreshTokenRepository.save(new RefreshTokenEntity(
                nextJti,
                parsed.userId(),
                parsed.tenantId(),
                parsed.familyId(),
                RefreshTokenStatus.ACTIVE,
                nextExp,
                now
        ));

        String nextAccess = tokenProviderPort.generateAccessToken(
                parsed.userId(),
                parsed.tenantId(),
                parsed.email(),
                parsed.roles()
        );
        // 4) 클라이언트에 새 access/refresh 쌍을 반환한다.
        return new RotatedTokens(nextAccess, nextRefresh);
    }
}
