package hsh.saas.saasapi.common.security.jwt;

/**
 * Refresh Token 수명주기 상태.
 *
 * <p>상태 전이는 대체로 다음 흐름을 따른다.
 * <ul>
 *   <li>ACTIVE -> ROTATED: 정상 회전 성공</li>
 *   <li>ACTIVE -> REVOKED: 강제 로그아웃/정책 폐기</li>
 *   <li>ROTATED/REVOKED 토큰 재사용 탐지 시 -> REUSED 표시</li>
 * </ul>
 */
public enum RefreshTokenStatus {
    /** 현재 사용 가능한 refresh 토큰 상태. */
    ACTIVE,

    /** 정상적으로 1회 사용 후 신규 토큰으로 교체된 상태. */
    ROTATED,

    /** 보안 정책 또는 사용자 액션으로 폐기된 상태. */
    REVOKED,

    /** 이미 소모/폐기된 토큰이 다시 들어온 재사용(replay) 탐지 상태. */
    REUSED
}
