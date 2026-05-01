package hsh.saas.saasapi.common.security.jwt;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.charset.StandardCharsets;

/**
 * JWT 설정 바인딩 객체.
 *
 * <p>`application.yml`의 `app.jwt.*` 값을 불변(record) 객체로 바인딩한다.
 * 본 타입은 "설정 값 보관 + 기동 시 검증"까지만 담당하고,
 * 실제 토큰 발급/검증 로직은 {@link JwtTokenProvider}가 수행한다.
 *
 * <p>예시:
 * <pre>
 * app:
 *   jwt:
 *     secret: "${APP_JWT_SECRET}"
 *     issuer: "subandadj-auth"
 *     audience: "subandadj-api"
 *     access-token-exp-seconds: 900
 *     refresh-token-exp-seconds: 1209600
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties (

        // HMAC 서명 비밀키. 운영에서는 평문 하드코딩 대신 시크릿 스토어 사용.
        @NotBlank(message = "app.jwt.secret must not be blank")
        String secret,

        // 토큰 발급자(iss). 환경/서비스 경계 식별에 사용.
        @NotBlank(message = "app.jwt.issuer must not be blank")
        String issuer,

        // 토큰 대상자(aud). 다른 클라이언트/서비스 토큰 유입 차단에 사용.
        @NotBlank(message = "app.jwt.audience must not be blank")
        String audience,

        // Access Token 만료(초).
        @Min(value = 60, message = "app.jwt.access-token-exp-seconds must be >= 60")
        long accessTokenExpSeconds,

        // Refresh Token 만료(초).
        @Min(value = 300, message = "app.jwt.refresh-token-exp-seconds must be >= 300")
        long refreshTokenExpSeconds
) {

    /**
     * 기동 시 secret 최소 길이를 검증한다.
     *
     * <p>HS256 사용 시 충분한 키 길이(최소 32바이트)를 강제해서
     * 짧은 키로 인한 보안 약화를 예방한다.
     * 검증 실패 시 애플리케이션은 시작되지 않는다(fail-fast).
     */
    @PostConstruct
    public void validateSecretStrength() {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
    }

    /*
     * TODO(P2): refresh token 만료값/issuer/audience 등 확장 설정 추가.
     * 완료조건: 발급 토큰의 클레임 정책이 환경별로 관리 가능하다.
     */
}
