package hsh.saas.saascore.security.jwtrefresh;

import java.util.List;

/**
 * Refresh token 관련 애플리케이션 유스케이스 경계.
 *
 * <p>이 인터페이스는 웹 계층이 core 정책을 호출할 때 사용하는 진입점이다.
 * 구현체는 트랜잭션과 상태 전이를 책임지고, 컨트롤러는 입출력(HTTP/쿠키)만 담당한다.
 */
public interface RefreshTokenUseCase {

    /**
     * 로그인 직후 최초 access/refresh 토큰 쌍을 발급한다.
     *
     * <p>최초 발급에서는 refresh family를 새로 만들고 상태를 ACTIVE로 저장한다.
     * 반환된 refresh 토큰은 보통 HttpOnly 쿠키로 내려준다.
     *
     * @param userId 인증된 사용자 ID
     * @param tenantId 사용자 소속 테넌트 ID
     * @param email access/refresh 클레임에 담을 사용자 이메일
     * @param roles access/refresh 클레임에 담을 권한 목록
     * @return 발급된 access/refresh 토큰 쌍
     * @throws IllegalArgumentException 파라미터가 정책상 유효하지 않은 경우
     */
    RotatedTokens issueInitialTokens(Long userId, Long tenantId, String email, List<String> roles);

    /**
     * 제출된 refresh 토큰을 1회 소비해 access/refresh를 재발급한다.
     *
     * <p>구 refresh 토큰은 ROTATED로 바뀌고, 새 refresh 토큰이 ACTIVE가 된다.
     * 이미 소모된 토큰이 다시 제출되면 replay로 간주해 예외를 던진다.
     *
     * @param presentedRefreshToken 클라이언트가 쿠키로 제출한 refresh 토큰
     * @return 재발급된 access/refresh 토큰 쌍
     * @throws IllegalArgumentException 저장소에 존재하지 않는 refresh 토큰인 경우
     * @throws IllegalStateException replay/만료 등 비정상 사용이 감지된 경우
     */
    RotatedTokens rotate(String presentedRefreshToken);
}
