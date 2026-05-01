package hsh.saas.saascore.security.jwtrefresh;

/**
 * 토큰 발급 결과 DTO.
 *
 * <p>로그인 또는 refresh 회전이 성공했을 때 반환되는 access/refresh 쌍이다.
 * API 계층에서는 보통 access는 JSON 바디로, refresh는 HttpOnly 쿠키로 내려준다.
 *
 * @param accessToken 보호 API 호출에 사용하는 단기 토큰
 * @param refreshToken access 재발급에 사용하는 장기 토큰
 */
public record RotatedTokens(String accessToken, String refreshToken) {
}

