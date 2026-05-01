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

/**
 * 구독 엔티티.
 *
 * <p>현재 모델은 최소 구성(소유 테넌트 + 생성시각)만 포함한다.
 * 향후 플랜, 상태, 결제 주기 등의 속성이 이 집계에 확장될 수 있다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

    /** 구독 식별자(PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 구독 소유 테넌트. 조회 성능을 위해 지연 로딩을 사용한다. */
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩, 구독 정보 조회 시 필요한 경우에만 고객사 정보 로딩
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** 감사 생성 시각. 최초 저장 이후 변경되지 않는다. */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 빌더 생성자.
     *
     * @param tenant 구독을 소유한 테넌트
     */
    @Builder
    public Subscription(Tenant tenant) {
        this.tenant = tenant;
    }

    /**
     * 연관관계 주인을 현재 구독으로 유지하면서 소유 테넌트를 교체한다.
     *
     * @param tenant 새 소유 테넌트
     */
    public void assignTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}

