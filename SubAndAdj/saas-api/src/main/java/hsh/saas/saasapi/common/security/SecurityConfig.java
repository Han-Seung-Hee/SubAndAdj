package hsh.saas.saasapi.common.security;

import hsh.saas.saasapi.common.security.filter.JwtAuthenticationFilter;
import hsh.saas.saasapi.common.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security의 전역 보안 규칙을 정의한다.
 *
 * <p>현재 설계 의도:
 * <ul>
 *   <li>세션 기반 인증을 사용하지 않는 Stateless API 서버로 동작</li>
 *   <li>JWT 필터가 모든 보호 API 요청 전에 실행되어 인증 컨텍스트를 구성</li>
 *   <li>로그인/헬스체크/초기 테넌트 등록만 공개, 그 외 API는 인증 필수</li>
 * </ul>
 *
 * <p>모듈 경계:
 * <ul>
 *   <li>saas-api: 웹/보안 어댑터 (현재 클래스 포함)</li>
 *   <li>saas-core: 도메인 규칙 (Tenant/User/Subscription 비즈니스 로직)</li>
 * </ul>
 */
@Configuration
// @PreAuthorize, @PostAuthorize 같은 메서드 단위 인가 어노테이션을 활성화한다.
@EnableMethodSecurity
// JwtProperties를 ConfigurationProperties 빈으로 등록해 주입 가능하게 만든다.
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * HTTP 보안 필터 체인을 구성한다.
     *
     * <p>핵심 정책:
     * <ul>
     *   <li>CSRF 비활성화: 브라우저 세션 기반이 아닌 JWT 기반 API이므로 기본 비활성화</li>
     *   <li>폼 로그인 비활성화: 커스텀 /auth API 사용</li>
     *   <li>Stateless 세션 정책: 서버 세션을 저장하지 않음</li>
     *   <li>JWT 필터 선배치: UsernamePasswordAuthenticationFilter 전에 토큰 인증 처리</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tenants").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /*
     * TODO(P1): 인증 실패/인가 실패 JSON 응답 포맷 통일 (AuthenticationEntryPoint, AccessDeniedHandler).
     * 완료조건: 401/403 응답이 API 공통 에러 스키마를 사용하고 traceId를 포함한다.
     *
     * TODO(P1): 공개 경로 재검토 및 Swagger/OpenAPI 경로 허용 정책 정의.
     * 완료조건: 운영에서 필요한 문서/헬스 엔드포인트만 공개된다.
     *
     * TODO(P2): Role 기반 URL 인가와 메서드 인가 기준을 문서화.
     * 완료조건: TENANT_ADMIN, TENANT_USER 권한 매트릭스가 코드/문서로 일치한다.
     */
}
