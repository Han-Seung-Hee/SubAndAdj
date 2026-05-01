package hsh.saas.saascore.security.jwtrefresh;

/**
 * Access/refresh token pair returned after successful rotation.
 */
public record RotatedTokens(String accessToken, String refreshToken) {
}

