package hsh.saas.saascore.security.jwtrefresh;

/**
 * Refresh token lifecycle status.
 */
public enum RefreshTokenStatus {
    ACTIVE,
    ROTATED,
    REVOKED,
    REUSED
}

