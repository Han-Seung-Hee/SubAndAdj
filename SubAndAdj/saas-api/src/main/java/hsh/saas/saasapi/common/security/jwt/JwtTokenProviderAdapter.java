package hsh.saas.saasapi.common.security.jwt;

import hsh.saas.saascore.security.jwtrefresh.ParsedRefreshToken;
import hsh.saas.saascore.security.jwtrefresh.TokenProviderPort;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

import java.util.List;

/**
  * Core 포트를 JWT 구현체로 연결하는 어댑터.
 *
 * <p>core는 JWT 라이브러리 타입(Claims/JwtException)을 모르고,
 * 오직 TokenProviderPort 계약만 호출한다. 이 클래스가 그 사이 변환을 담당한다.
 * 어댑터 계층에서 클레임 누락을 조기에 검증해 core 입력을 방어한다.
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    /** 실제 JWT 구현체(JJWT 기반). */
    private final JwtTokenProvider jwtTokenProvider;

    public JwtTokenProviderAdapter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** core 요청에 따라 access 토큰을 발급한다. */
    @Override
    public String generateAccessToken(Long userId, Long tenantId, String email, List<String> roles) {
        return jwtTokenProvider.generateAccessToken(userId, tenantId, email, roles);
    }

    /** core 요청에 따라 refresh 토큰을 발급한다. */
    @Override
    public String generateRefreshToken(Long userId, Long tenantId, String email, List<String> roles, String familyId, String jti) {
        return jwtTokenProvider.generateRefreshToken(userId, tenantId, email, roles, familyId, jti);
    }

    /**
     * refresh JWT 문자열을 core 전용 DTO로 변환한다.
     *
     * <p>여기서 클레임을 방어적으로 검사해 누락된 토큰이 core로 전달되지 않게 한다.
     * 검증 실패는 IllegalArgumentException으로 표준화해 상위에서 일관 처리한다.
     */
    @Override
    public ParsedRefreshToken parseRefreshToken(String token) {
        Claims claims = jwtTokenProvider.parseRefreshToken(token);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(JwtTokenProvider.CLAIM_ROLES, List.class);

        String email = claims.get(JwtTokenProvider.CLAIM_EMAIL, String.class);
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Missing refresh claim: email");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Missing refresh claim: roles");
        }
        // core 회전 로직이 바로 사용할 수 있도록 라이브러리 독립 DTO로 변환한다.

        return new ParsedRefreshToken(
                claims.getId(),
                claims.get(JwtTokenProvider.CLAIM_FAMILY_ID, String.class),
                Long.valueOf(claims.getSubject()),
                claims.get(JwtTokenProvider.CLAIM_TENANT_ID, Long.class),
                email,
                roles,
                claims.getExpiration().toInstant()
        );
    }
}
