package hsh.saas.saasapi.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 발급/검증 유틸리티.
 *
 * <p>이 컴포넌트는 애플리케이션 내부에서 사용할 JWT Access/Refresh 토큰의
 * 생성과 기본 검증(서명, 만료, 발급자, 대상자, 타입)을 담당한다.
 *
 * <p><b>책임 범위</b>
 * <ul>
 *   <li>Access Token 발급</li>
 *   <li>Refresh Token 발급</li>
 *   <li>토큰 파싱 및 필수 클레임 검증</li>
 * </ul>
 *
 * <p><b>책임 범위 밖</b>
 * <ul>
 *   <li>키 로테이션(kid 기반 다중 키 선택)</li>
 *   <li>Refresh Token 회전 상태 저장/재사용 탐지(서버 저장소 기반)</li>
 *   <li>토큰 폐기(blacklist/revocation) 영속화</li>
 * </ul>
 *
 * <p>즉, "토큰 자체의 암호학적 유효성"은 검증하지만,
 * "토큰 사용 이력 기반의 정책 검증"은 별도 서비스/저장소가 필요하다.
 */
@Component
public class JwtTokenProvider {

    /**
     * 커스텀 클레임 키: 사용자가 속한 테넌트 식별자.
     * 멀티테넌트 권한 범위를 결정할 때 사용한다.
     */
    public static final String CLAIM_TENANT_ID = "tenantId";

    /**
     * 커스텀 클레임 키: 사용자 이메일.
     * 주로 표시/감사 로깅 목적이며, 인가 판단의 주키로는 sub(userId)를 우선 사용한다.
     */
    public static final String CLAIM_EMAIL = "email";

    /**
     * 커스텀 클레임 키: 권한(Role) 목록.
     * 인증 필터/인가 계층에서 권한 매핑 시 사용한다.
     */
    public static final String CLAIM_ROLES = "roles";

    /**
     * 커스텀 클레임 키: 토큰 타입(access/refresh).
     * Access 엔드포인트에 Refresh 토큰이 들어오는 실수를 차단하기 위한 안전장치다.
     */
    public static final String CLAIM_TYP = "typ";

    /** Access Token 식별 문자열. */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Refresh Token 식별 문자열. */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /**
     * 커스텀 클레임 키: refresh token family 식별자.
     * 같은 로그인 세션 체인(회전 계보)을 묶어 replay 탐지 시 일괄 폐기 범위를 결정한다.
     */
    public static final String CLAIM_FAMILY_ID = "familyId";

    /**
     * HMAC 서명/검증 키.
     * 모든 토큰은 동일 키로 서명되며, 키 교체 전략은 TODO(P2) 범위다.
     */
    private final SecretKey key;

    /** JWT 관련 설정(issuer, audience, 만료시간, secret 등). */
    private final JwtProperties props;

    /**
     * 설정값의 secret 문자열을 HMAC 서명 키로 변환한다.
     *
     * <p>주의: secret 길이는 알고리즘 요구사항(충분한 엔트로피/길이)을 만족해야 하며,
     * 짧거나 예측 가능한 문자열은 보안상 취약하다.
     */
    public JwtTokenProvider(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token을 발급한다.
     *
     * <p>Access Token은 API 인증/인가에 직접 사용되므로,
     * 역할/사용자 식별 정보(roles, sub, tenantId, email)를 포함한다.
     *
     * @param userId 인증 주체 사용자 ID (JWT 표준 sub 클레임으로 저장)
     * @param tenantId 현재 요청 문맥의 테넌트 ID
     * @param email 사용자 식별용 이메일
     * @param roles 인가에 사용할 권한 목록
     * @return 서명된 Access Token 문자열
     */
    public String generateAccessToken(Long userId, Long tenantId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.accessTokenExpSeconds());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(props.issuer())
                .audience().add(props.audience()).and()
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_TENANT_ID, tenantId)
                .claim(CLAIM_TYP, TOKEN_TYPE_ACCESS)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token을 발급한다.
     *
     * <p>Refresh Token은 access 재발급 전용 토큰으로, 노출 면적을 줄이기 위해
     * 권한 목록 같은 인가 데이터는 싣지 않고 식별/회전 정책 데이터만 포함한다.
     *
     * @param userId 인증 주체 사용자 ID (sub)
     * @param tenantId 현재 요청 문맥의 테넌트 ID
     * @param email access 재발급 시 재조회 없이 사용할 사용자 이메일
     * @param roles access 재발급 시 재조회 없이 사용할 권한 목록
     * @param familyId 회전 체인을 식별하는 세션 계보 ID
     * @param jti 이번 refresh 토큰의 고유 식별자
     * @return 서명된 Refresh Token 문자열
     */
    public String generateRefreshToken(Long userId, Long tenantId, String email, List<String> roles, String familyId, String jti) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.refreshTokenExpSeconds());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(props.issuer())
                .audience().add(props.audience()).and()
                .id(jti)
                .claim(CLAIM_TYP, TOKEN_TYPE_REFRESH)
                .claim(CLAIM_TENANT_ID, tenantId)
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_FAMILY_ID, familyId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    /**
     * Access Token 전용 파서.
     *
     * <p>공통 검증(서명/만료/iss/aud) 이후 typ=access를 강제해서
     * refresh 토큰의 보호 API 오용을 차단한다.
     */
    public Claims parseAccessToken(String token) {
        Claims claims = parseSignedClaims(token);
        validateType(claims, TOKEN_TYPE_ACCESS);
        validateRequiredClaims(claims);
        return claims;
    }

    /**
     * Refresh Token 전용 파서.
     *
     * <p>공통 검증(서명/만료/iss/aud) 이후 typ=refresh를 강제해서
     * access 토큰과 역할이 뒤섞이는 실수를 막는다.
     */
    public Claims parseRefreshToken(String token) {
        Claims claims = parseSignedClaims(token);
        validateType(claims, TOKEN_TYPE_REFRESH);
        validateRequiredClaims(claims);
        return claims;
    }

    /**
     * 기존 호출부 호환용 메서드.
     *
     * <p>과거 parse(token) 사용 코드를 깨지 않기 위해 유지하며,
     * 내부적으로 Access Token 파서로 위임한다.
     */
    public Claims parse(String token) {
        return parseAccessToken(token);
    }

    /**
     * 토큰의 공통 서명/구조/만료/발급자/대상자 검증을 수행하고 Claims를 반환한다.
     *
     * <p>이 단계에서는 typ(access/refresh) 구분을 하지 않는다.
     * 타입 검증은 parseAccessToken/parseRefreshToken에서 수행한다.
     */
    public Claims parseSignedClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .requireAudience(props.audience())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * typ 클레임이 기대값과 일치하는지 검증한다.
     *
     * @throws JwtException access/refresh 교차 사용 등 정책 위반 시
     */
    private void validateType(Claims claims, String expectedType) {
        String typ = claims.get(CLAIM_TYP, String.class);
        if (!expectedType.equals(typ)) {
            throw new JwtException("Invalid token type: " + typ);
        }
    }

    /**
     * 인증 컨텍스트 구성에 반드시 필요한 표준 클레임 존재 여부를 점검한다.
     *
     * <ul>
     *   <li>jti: 토큰 고유 식별자 (재사용 탐지/폐기 정책 연동용)</li>
     *   <li>sub: 인증 주체 식별자</li>
     * </ul>
     */
    private void validateRequiredClaims(Claims claims) {
        if (claims.getId() == null || claims.getId().isBlank()) {
            throw new JwtException("Missing jti");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtException("Missing sub");
        }
    }

    /*
     * TODO(P1): issuer(aud/iss), token type, jti 등 표준 클레임을 추가하고 검증한다.
     * 완료조건: 다른 서비스/환경에서 온 토큰을 정책적으로 거부할 수 있다.
     *
     * TODO(P1): Refresh Token 발급/검증 로직 분리 및 회전(rotation) 지원.
     * 완료조건: 재사용(refresh token replay) 탐지가 가능하다.
     *
     * TODO(P2): 키 로테이션(kid 헤더) 및 다중 키 검증 전략 도입.
     * 완료조건: 무중단으로 키 교체가 가능하다.
     */

}
