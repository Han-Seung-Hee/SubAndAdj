package hsh.saas.saascore.domain;

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
// 고객사 엔티티

@Entity
@Table(name = "tenants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한. 프록시 생성
/*
 프록시란
    - 실제 객체 대신에 그 객체를 대신하는 가짜 객체를 말한다.
    - 프록시는 실제 객체에 대한 접근을 제어하거나, 추가적인 기능을 제공하기 위해 사용된다.
    - 예를 들어, 데이터베이스에서 엔티티를 조회할 때, JPA는 실제 엔티티 객체 대신에 프록시 객체를 반환할 수 있다. 이
        프록시 객체는 실제 엔티티의 데이터를 로드하기 전까지는 데이터베이스에 접근하지 않고, 필요한 시점에 데이터를 로드한다.
 */
@EntityListeners(AuditingEntityListener.class) // 생성 , 수정시간 자동기록
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     *  보유한 구독 목록 ( 1 : N, 고객은 여러개의 구독을 가지고 있을 수 있음 )
     *  - mappedBy = 연관관계의 주인이 아님. Subscription 엔티티의 tenant 필드가 주인.
     *  - cascade = CascadeType.ALL : Tenant 저장 / 삭제 시  구독 정보도 함께 처리
     *  - orphanRemoval = true : 부모의 연결이 끊긴 구독 정보를 자동으로 삭제.
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // use jpa auditing
    @LastModifiedDate
    private LocalDateTime updateAt;

    @Builder // 빌더 패턴을 사용하여 객체 생성
    public Tenant(String name){
        this.name = name;
    }

    // --- 비즈니스 로직 및 연관 관계 편의 메서드 ---

    /**
     * 새로운 구독을 추가할 때 양방향 객체 상태를 동기화합니다.
     * @param subscription 추가할 구독 엔티티
     */
    public void addSubscription(Subscription subscription) {
        this.subscriptions.add(subscription);
        // 무한 루프 방지를 위한 조건부 체크
        if (subscription.getTenant() != this) {
            subscription.assignTenant(this);
        }
    }
}
