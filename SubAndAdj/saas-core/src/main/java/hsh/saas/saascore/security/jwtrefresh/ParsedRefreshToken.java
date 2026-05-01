package hsh.saas.saascore.security.jwtrefresh;

import java.time.Instant;
import java.util.List;

/**
 * Parsed refresh token payload exposed to core without leaking JWT library types.
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

