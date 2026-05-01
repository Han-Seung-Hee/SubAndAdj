package hsh.saas.saascore.security.jwtrefresh;

import java.util.List;

/**
 * Port abstracting JWT generation/parsing from core rotation policy.
 */
public interface TokenProviderPort {

    String generateAccessToken(Long userId, Long tenantId, String email, List<String> roles);

    String generateRefreshToken(Long userId, Long tenantId, String email, List<String> roles, String familyId, String jti);

    ParsedRefreshToken parseRefreshToken(String token);
}

