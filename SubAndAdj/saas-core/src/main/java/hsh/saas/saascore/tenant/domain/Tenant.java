package hsh.saas.saascore.tenant.domain;

import hsh.saas.saascore.subscription.domain.Subscription;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 테넌트(고객사) 집계 루트 엔티티.
 *
 * <p>구독 목록의 부모이며, 생성/수정 시각은 JPA Auditing으로 자동 관리한다.
 */
@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한. 프록시 생성
@EntityListeners(AuditingEntityListener.class) // 생성, 수정시간 자동기록
public class Tenant {
    /** 테넌트 식별자(PK). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 테넌트 표시명. 시스템 전역 유일값으로 관리한다. */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 보유한 구독 목록 (1:N, 고객은 여러 개의 구독을 가질 수 있음)
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    /** 감사 생성 시각. 최초 저장 시 자동 기록된다. */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 감사 수정 시각. 변경 시마다 자동 갱신된다. */
    @LastModifiedDate
    private LocalDateTime updateAt;

    /**
     * 빌더 생성자.
     *
     * @param name 테넌트 이름
     */
    @Builder
    public Tenant(String name) {
        this.name = name;
    }

    /**
     * 새로운 구독 추가 시 양방향 연관관계를 동기화합니다.
     */
    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
        if (subscription.getTenant() != this) {
            subscription.assignTenant(this);
        }
    }
}

