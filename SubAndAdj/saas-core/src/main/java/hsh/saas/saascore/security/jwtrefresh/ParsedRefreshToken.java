package hsh.saas.saascore.security.jwtrefresh;

import java.time.Instant;
import java.util.List;

/**
 * Parsed refresh token payload exposed to core without leaking JWT library types.
 *
 * @param jti refresh 토큰 고유 식별자
 * @param familyId refresh 회전 계보 식별자
 * @param userId 사용자 ID
 * @param tenantId 테넌트 ID
 * @param email 사용자 이메일
 * @param roles access 재발급에 재사용할 권한 목록
 * @param expiresAt refresh 토큰 만료 시각
 */
public record ParsedRefreshToken(
        String jti,
        String familyId,
        Long userId,
        Long tenantId,
        String email,
        List<String> roles,
        Instant expiresAt
) {
}

