package hsh.saas.saasapi.common.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * 인증 토큰 재발급 API.
 *
 * <p>refresh 토큰은 헤더/바디가 아니라 HttpOnly 쿠키로만 전달받아
 * XSS 환경에서 토큰 탈취 표면을 줄인다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthTokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtProperties jwtProperties;

    public AuthTokenController(RefreshTokenService refreshTokenService, JwtProperties jwtProperties) {
        this.refreshTokenService = refreshTokenService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * refresh 토큰 회전을 수행하고 새 access 토큰을 반환한다.
     *
     * <p>응답 본문에는 access token만 내려주고,
     * refresh token은 HttpOnly + Secure + SameSite=Strict 쿠키로 갱신한다.
     */
    @PostMapping("/refresh")
    public AccessTokenResponse refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        // TODO: 실제 운영에서는 userId 기반으로 DB에서 이메일/권한을 재조회해야 한다.
        // 현재 값은 흐름 검증을 위한 임시 샘플이다.
        String email = "from-db@example.com";
        List<String> roles = List.of("USER");

        var rotated = refreshTokenService.rotate(refreshToken, email, roles);

        ResponseCookie cookie = ResponseCookie.from("refresh_token", rotated.refreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(jwtProperties.refreshTokenExpSeconds()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return new AccessTokenResponse(rotated.accessToken());
    }

    /** 재발급 응답 DTO: access token만 노출한다. */
    public record AccessTokenResponse(String accessToken) {}
}