package hsh.saas.saasapi.common.security.auth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT 검증이 끝난 뒤 SecurityContext에 저장되는 인증 객체.
 *
 * <p>이 객체는 "이미 인증된 사용자 정보"를 표현한다.
 * 비밀번호 같은 원본 자격증명은 보관하지 않으며,
 * 요청 처리 중 필요한 최소 식별 정보(userId, tenantId, email)만 유지한다.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Long userId;
    private final Long tenantId;
    private final String email;

    /**
     * @param authorities 권한 목록 (예: TENANT_ADMIN, TENANT_USER)
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @param email 사용자 이메일
     */
    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Long userId, Long tenantId, String email) {
        super(authorities);
        this.userId = userId;
        this.tenantId = tenantId;
        this.email = email;
        // JWT 서명/만료 검증 이후 생성되는 객체이므로 인증 완료 상태로 표시한다.
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        // JWT 기반 구조에서는 원본 자격증명을 보관하지 않는다.
        return "";
    }

    @Override
    public Object getPrincipal() {
        // principal은 애플리케이션 기준 대표 식별자(userId)로 사용한다.
        return userId;
    }

    public Long getUserId(){
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    /*
     * TODO(P2): principal을 별도 Principal 객체로 승격해 타입 안정성을 강화한다.
     * 완료조건: 컨트롤러/서비스에서 캐스팅 없이 사용자 컨텍스트를 조회할 수 있다.
     */
}
