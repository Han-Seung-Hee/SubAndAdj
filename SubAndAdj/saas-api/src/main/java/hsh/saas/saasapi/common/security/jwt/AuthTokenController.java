package hsh.saas.saasapi.common.security.jwt;

import hsh.saas.saascore.security.jwtrefresh.RefreshTokenUseCase;
import hsh.saas.saascore.security.jwtrefresh.RotatedTokens;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * 인증 토큰 발급/재발급 API.
 *
 * <p>역할 분리:
 * <ul>
 *   <li>core: refresh 상태 전이/회전 정책</li>
 *   <li>api(현재 클래스): HTTP 입출력, 쿠키 생성, 요청 검증</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthTokenController {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final JwtProperties jwtProperties;

    public AuthTokenController(RefreshTokenUseCase refreshTokenUseCase, JwtProperties jwtProperties) {
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 최초 로그인 후 access/refresh 토큰을 동시에 발급한다.
     *
     * <p>현재 프로젝트에는 사용자 검증(아이디/비밀번호 인증) 모듈이 아직 없으므로,
     * 이미 인증 완료된 사용자 정보를 상위 계층/외부 인증 모듈이 전달한다는 전제로 동작한다.
     */
    @PostMapping("/login")
    public AccessTokenResponse login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        RotatedTokens issued = refreshTokenUseCase.issueInitialTokens(
                request.userId(),
                request.tenantId(),
                request.email(),
                request.roles()
        );

        addRefreshCookie(response, issued.refreshToken());
        return new AccessTokenResponse(issued.accessToken());
    }

    /**
     * refresh 토큰을 1회 소비해 access/refresh를 재발급한다.
     *
     * <p>응답 바디에는 access token만 내리고,
     * refresh token은 HttpOnly 쿠키로 교체한다.
     */
    @PostMapping("/refresh")
    public AccessTokenResponse refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        RotatedTokens rotated = refreshTokenUseCase.rotate(refreshToken);

        addRefreshCookie(response, rotated.refreshToken());
        return new AccessTokenResponse(rotated.accessToken());
    }

    /**
     * refresh token 쿠키를 표준 정책으로 설정한다.
     *
     * <ul>
     *   <li>HttpOnly: JS 접근 차단</li>
     *   <li>Secure: HTTPS 전송만 허용</li>
     *   <li>SameSite=Strict: 크로스 사이트 요청 전송 억제</li>
     * </ul>
     */
    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(jwtProperties.refreshTokenExpSeconds()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** 로그인 입력 DTO(상위 인증 계층이 검증 완료한 사용자 컨텍스트). */
    public record LoginRequest(
            @NotNull Long userId,
            @NotNull Long tenantId,
            @NotBlank String email,
            @NotEmpty List<String> roles
    ) {}

    /** 재발급/로그인 공통 응답 DTO: access token만 노출한다. */
    public record AccessTokenResponse(String accessToken) {}
}