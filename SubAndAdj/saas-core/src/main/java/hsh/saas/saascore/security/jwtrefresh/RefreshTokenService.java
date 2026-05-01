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
     */
    @Override
    @Transactional
    public RotatedTokens issueInitialTokens(Long userId, Long tenantId, String email, List<String> roles) {
        String familyId = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();

        String refreshToken = tokenProviderPort.generateRefreshToken(userId, tenantId, email, roles, familyId, jti);
        Instant exp = tokenProviderPort.parseRefreshToken(refreshToken).expiresAt();

        refreshTokenRepository.save(new RefreshTokenEntity(
                jti,
                userId,
                tenantId,
                familyId,
                RefreshTokenStatus.ACTIVE,
                exp,
                Instant.now()
        ));

        String accessToken = tokenProviderPort.generateAccessToken(userId, tenantId, email, roles);
        return new RotatedTokens(accessToken, refreshToken);
    }

    /**
     * refresh 토큰을 1회 소비해 access/refresh를 재발급한다.
     *
     * <p>동시성 핵심:
     * `findByJtiForUpdate`가 행 락을 잡아 동일 refresh 토큰 동시 제출 시 1건만 성공한다.
     */
    @Override
    @Transactional
    public RotatedTokens rotate(String presentedRefreshToken) {
        ParsedRefreshToken parsed = tokenProviderPort.parseRefreshToken(presentedRefreshToken);

        RefreshTokenEntity current = refreshTokenRepository.findByJtiForUpdate(parsed.jti())
                .orElseThrow(() -> new IllegalArgumentException("Unknown refresh token"));

        Instant now = Instant.now();

        // ACTIVE가 아니거나 만료면 재사용/비정상으로 간주하고 family 단위로 차단한다.
        if (current.getStatus() != RefreshTokenStatus.ACTIVE || current.isExpired(now)) {
            refreshTokenRepository.revokeActiveByFamily(current.getFamilyId(), RefreshTokenStatus.REVOKED, now);
            current.markReused(now);
            throw new IllegalStateException("Refresh token replay detected");
        }

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
        return new RotatedTokens(nextAccess, nextRefresh);
    }
}
