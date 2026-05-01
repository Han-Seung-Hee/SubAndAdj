package hsh.saas.saascore.security.jwtrefresh;

/**
 * Refresh token 수명주기 상태.
 */
public enum RefreshTokenStatus {
    /** 정상 사용 가능한 현재 토큰. */
    ACTIVE,
    /** 정상 회전으로 후속 토큰이 발급되어 더 이상 직접 사용할 수 없는 상태. */
    ROTATED,
    /** 보안 이벤트(로그아웃/가족 폐기 등)로 정책적으로 차단된 상태. */
    REVOKED,
    /** 이미 소비된 토큰이 다시 제출되어 replay로 판정된 상태. */
    REUSED
}

