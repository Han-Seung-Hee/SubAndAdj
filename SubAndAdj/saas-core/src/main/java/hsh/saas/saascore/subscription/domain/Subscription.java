package hsh.saas.saascore.subscription.domain;

import hsh.saas.saascore.tenant.domain.Tenant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩, 구독 정보 조회 시 필요한 경우에만 고객사 정보 로딩
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Subscription(Tenant tenant) {
        this.tenant = tenant;
    }

    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}

