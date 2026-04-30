package hsh.saas.saasapi.common.security.filter;

import hsh.saas.saasapi.common.security.auth.JwtAuthenticationToken;
import hsh.saas.saasapi.common.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 요청의 Authorization 헤더(Bearer Token)를 읽어 JWT 인증을 수행하는 필터.
 *
 * <p>처리 순서:
 * <ol>
 *   <li>Authorization 헤더 존재/형식(Bearer) 확인</li>
 *   <li>JwtTokenProvider로 서명/만료 검증</li>
 *   <li>Claims에서 사용자/테넌트/권한 정보를 추출</li>
 *   <li>JwtAuthenticationToken 생성 후 SecurityContext에 저장</li>
 * </ol>
 *
 * <p>실패 전략:
 * 토큰 파싱이 실패하면 컨텍스트를 비우고 다음 필터로 진행한다.
 * 이후 Security 규칙에서 인증 실패(401)로 처리된다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        // RFC 표준 헤더: "Authorization: Bearer <token>"
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try{
                // 1) 토큰 서명/만료 검증
                Claims claims = jwtTokenProvider.parse(token);

                // 2) 도메인 인증 컨텍스트에 필요한 정보 추출
                Long userId = Long.valueOf(claims.getSubject());
                Long tenantId = ((Number) claims.get("tenantId")).longValue();
                String email = claims.get("email", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                // 3) Spring Security 권한 객체로 변환
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                // 4) 인증 객체를 SecurityContext에 등록
                var authentication = new JwtAuthenticationToken(authorities, userId, tenantId, email);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(Exception ignored){
                // 토큰이 위조/만료/형식오류인 경우 인증 정보를 제거하고 다음 체인으로 진행
                SecurityContextHolder.clearContext();
            }
        }

        // 다음 필터로 반드시 요청을 전달한다.
        filterChain.doFilter(request,response);
    }

    /*
     * TODO(P1): /api/auth/**, /actuator/health 등 공개 경로는 필터 스킵(shouldNotFilter)으로 최적화.
     * 완료조건: 공개 경로 호출 시 불필요한 토큰 파싱이 발생하지 않는다.
     *
     * TODO(P1): 예외 유형별 로깅/메트릭(만료, 서명불일치, malformed) 분리.
     * 완료조건: 운영에서 인증 실패 원인을 관측할 수 있다.
     *
     * TODO(P1): tenantId/roles 클레임 누락 시 명시적으로 인증 실패 처리.
     * 완료조건: 불완전 토큰이 우회되지 않는다.
     */
}
