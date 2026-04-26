package hsh.saas.saasapi.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Refresh Token 발급/회전 서비스.
 *
 * <p>이 서비스는 다음 정책을 구현한다.
 * <ul>
 *   <li>refresh 토큰 1회성 소비</li>
 *   <li>정상 회전 시 old -> ROTATED, new -> ACTIVE</li>
 *   <li>재사용(replay) 탐지 시 family 단위 ACTIVE 토큰 폐기</li>
 * </ul>
 */
@Service
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(JwtTokenProvider jwtTokenProvider,
                               RefreshTokenRepository refreshTokenRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 로그인 직후 최초 refresh 토큰을 발급하고 ACTIVE 상태로 저장한다.
     */
    @Transactional
    public String issueInitialRefreshToken(Long userId, Long tenantId) {
        String familyId = UUID.randomUUID().toString();
        String jti = UUID.randomUUID().toString();

        String token = jwtTokenProvider.generateRefreshToken(userId, tenantId, familyId, jti);
        Instant exp = jwtTokenProvider.parseRefreshToken(token).getExpiration().toInstant();

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        jti, userId, tenantId, familyId,
                        RefreshTokenStatus.ACTIVE, exp, Instant.now()
                )
        );
        return token;
    }

    /**
     * refresh 토큰을 검증하고 access/refresh를 함께 재발급한다.
     *
     * <p>동시성 보장 포인트:
     * `findByJtiForUpdate`가 행 락을 걸기 때문에 같은 토큰 2건 동시 요청 시
     * 첫 요청만 ACTIVE 상태에서 회전에 성공하고, 나머지는 상태 변경 이후 실패한다.
     *
     * @param presentedRefreshToken 클라이언트가 제출한 refresh 토큰(JWT)
     * @param email 새 access 토큰에 실을 이메일
     * @param roles 새 access 토큰에 실을 권한 목록
     * @return 새 access/refresh 토큰 쌍
     */
    @Transactional
    public RotatedResult rotate(String presentedRefreshToken, String email, List<String> roles) {
        Claims claims = jwtTokenProvider.parseRefreshToken(presentedRefreshToken);

        String currentJti = claims.getId();
        String familyId = claims.get(JwtTokenProvider.CLAIM_FAMILY_ID, String.class);
        Long userId = Long.valueOf(claims.getSubject());
        Long tenantId = claims.get(JwtTokenProvider.CLAIM_TENANT_ID, Long.class);

        RefreshTokenEntity current = refreshTokenRepository.findByJtiForUpdate(currentJti)
                .orElseThrow(() -> new JwtException("Unknown refresh token"));

        Instant now = Instant.now();

        // 이미 소모/만료된 토큰이면 replay 또는 비정상 상태로 간주한다.
        if (current.getStatus() != RefreshTokenStatus.ACTIVE || current.isExpired(now)) {
            refreshTokenRepository.revokeActiveByFamily(current.getFamilyId(), RefreshTokenStatus.REVOKED, now);
            current.markReused(now);
            throw new JwtException("Refresh token replay detected");
        }

        String nextJti = UUID.randomUUID().toString();
        String nextRefresh = jwtTokenProvider.generateRefreshToken(userId, tenantId, familyId, nextJti);
        Instant nextExp = jwtTokenProvider.parseRefreshToken(nextRefresh).getExpiration().toInstant();

        current.markRotated(nextJti, now);

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        nextJti, userId, tenantId, familyId,
                        RefreshTokenStatus.ACTIVE, nextExp, now
                )
        );

        String nextAccess = jwtTokenProvider.generateAccessToken(userId, tenantId, email, roles);
        return new RotatedResult(nextAccess, nextRefresh);
    }

    /** access/refresh 쌍 반환 DTO. */
    public record RotatedResult(String accessToken, String refreshToken) {}
}
