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
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "family_id", nullable = false, length = 64)
    private String familyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private RefreshTokenStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "rotated_to_jti", length = 64)
    private String rotatedToJti;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    protected RefreshTokenEntity() {
    }

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

    public boolean isExpired(Instant now) {
        return expiresAt.isBefore(now);
    }

    public void markRotated(String nextJti, Instant now) {
        this.status = RefreshTokenStatus.ROTATED;
        this.rotatedToJti = nextJti;
        this.lastUsedAt = now;
    }

    public void markReused(Instant now) {
        this.status = RefreshTokenStatus.REUSED;
        this.lastUsedAt = now;
    }

    public void markRevoked(Instant now) {
        this.status = RefreshTokenStatus.REVOKED;
        this.lastUsedAt = now;
    }
}

