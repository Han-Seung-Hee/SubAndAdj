package hsh.saas.saascore.security.jwtrefresh;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Persisted refresh token state for rotation and replay detection.
 *
 * <p>refresh 토큰은 무상태 JWT이지만, 재사용 탐지/회전 정책을 위해
 * 서버 상태를 별도로 유지한다. 이 엔티티는 그 상태 저장 단위다.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    /** refresh 토큰 고유 식별자(JWT jti). PK로 사용한다. */
    @Id
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    /** 토큰 소유 사용자 ID. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 토큰 소유 테넌트 ID. */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 회전 체인(동일 로그인 계보) 식별자. */
    @Column(name = "family_id", nullable = false, length = 64)
    private String familyId;

    /** refresh 수명주기 상태(ACTIVE/ROTATED/REVOKED/REUSED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RefreshTokenStatus status;

    /** JWT 만료시각(exp)을 서버 기준으로 보관한 값. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** 정상 회전 시 다음 토큰의 jti를 연결해 체인을 추적할 수 있게 한다. */
    @Column(name = "rotated_to_jti", length = 64)
    private String rotatedToJti;

    /** 최초 저장 시각. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 마지막 상태 전이 시각(회전/재사용/폐기). */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    /** JPA 프록시용 보호 생성자. */
    protected RefreshTokenEntity() {
    }

    /**
     * 새 refresh 상태 레코드를 생성한다.
     */
    public RefreshTokenEntity(String jti, Long userId, Long tenantId, String familyId,
                              RefreshTokenStatus status, Instant expiresAt, Instant createdAt) {
        this.jti = jti;
        this.userId = userId;
        this.tenantId = tenantId;
        this.familyId = familyId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getJti() { return jti; }
    public Long getUserId() { return userId; }
    public Long getTenantId() { return tenantId; }
    public String getFamilyId() { return familyId; }
    public RefreshTokenStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }

    /**
     * 현재 시각 기준 만료 여부를 판정한다.
     */
    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    /**
     * 정상 회전을 반영한다.
     *
     * @param nextJti 새로 발급된 refresh의 jti
     * @param now 상태 전이 시각
     */
    public void markRotated(String nextJti, Instant now) {
        this.status = RefreshTokenStatus.ROTATED;
        this.rotatedToJti = nextJti;
        this.lastUsedAt = now;
    }

    /**
     * 재사용(replay) 감지 상태로 변경한다.
     */
    public void markReused(Instant now) {
        this.status = RefreshTokenStatus.REUSED;
        this.lastUsedAt = now;
    }

    /**
     * 정책상 폐기 상태로 변경한다.
     */
    public void markRevoked(Instant now) {
        this.status = RefreshTokenStatus.REVOKED;
        this.lastUsedAt = now;
    }
}

