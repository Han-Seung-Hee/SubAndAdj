package hsh.saas.saascore.security.jwtrefresh;

import java.util.List;

/**
 * Port abstracting JWT generation/parsing from core rotation policy.
 *
 * <p>핵심 목적은 core가 특정 JWT 라이브러리 타입에 결합되지 않도록 하는 것이다.
 * 구현체는 saas-api 어댑터 계층에 존재하며, core는 이 계약만 사용한다.
 * 회전 정책은 이 포트의 결과를 신뢰 가능한 입력으로 사용한다.
 */
public interface TokenProviderPort {

    /**
     * access 토큰을 발급한다.
     *
     * @param userId 사용자 ID(sub)
     * @param tenantId 테넌트 ID
     * @param email 이메일 클레임
     * @param roles 권한 목록 클레임
     * @return 서명된 access JWT
     */
    String generateAccessToken(Long userId, Long tenantId, String email, List<String> roles);

    /**
     * refresh 토큰을 발급한다.
     *
     * @param userId 사용자 ID(sub)
     * @param tenantId 테넌트 ID
     * @param email 이메일 클레임
     * @param roles 권한 목록 클레임
     * @param familyId refresh 회전 계보 식별자
     * @param jti refresh 토큰 고유 식별자
     * @return 서명된 refresh JWT
     */
    String generateRefreshToken(Long userId, Long tenantId, String email, List<String> roles, String familyId, String jti);

    /**
     * refresh JWT를 core 전용 DTO로 파싱한다.
     *
     * @param token refresh JWT 문자열
     * @return core가 바로 사용할 수 있는 파싱 결과
     */
    ParsedRefreshToken parseRefreshToken(String token);
}

