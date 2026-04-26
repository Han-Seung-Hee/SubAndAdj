package hsh.saas.saasapi.common.security.jwt;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Refresh Token 상태 저장 엔티티.
 *
 * <p>JWT 원문 자체를 저장하지 않고, jti(토큰 ID)와 상태를 기준으로
 * 회전/재사용 탐지 정책을 수행하기 위한 메타데이터를 저장한다.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    /** JWT ID (jti). refresh 토큰 1개를 유일하게 식별한다. */
    @Id
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    /** 토큰 소유 사용자 ID. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 토큰 소유 테넌트 ID. */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /** 회전 체인 식별자. replay 탐지 시 family 단위 폐기에 사용한다. */
    @Column(name = "family_id", nullable = false, length = 64)
    private String familyId;

    /** 현재 토큰 상태(ACTIVE/ROTATED/REVOKED/REUSED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RefreshTokenStatus status;

    /** 토큰 만료 시각. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** 회전된 다음 토큰의 jti. 체인 추적이 필요할 때 사용한다. */
    @Column(name = "rotated_to_jti", length = 64)
    private String rotatedToJti;

    /** 생성 시각. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** 마지막 사용 시각(정상 사용/재사용 탐지 시점 포함). */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected RefreshTokenEntity() {}

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

    /** 현재 시각 기준으로 만료 여부를 확인한다. */
    public boolean isExpired(Instant now) { return expiresAt.isBefore(now); }

    /** 정상 회전에 성공한 토큰 상태로 마킹한다. */
    public void markRotated(String nextJti, Instant now) {
        this.status = RefreshTokenStatus.ROTATED;
        this.rotatedToJti = nextJti;
        this.lastUsedAt = now;
    }

    /** 이미 소모된 토큰 재사용이 탐지된 상태로 마킹한다. */
    public void markReused(Instant now) {
        this.status = RefreshTokenStatus.REUSED;
        this.lastUsedAt = now;
    }

    /** 강제 폐기 상태로 마킹한다. */
    public void markRevoked(Instant now) {
        this.status = RefreshTokenStatus.REVOKED;
        this.lastUsedAt = now;
    }
}