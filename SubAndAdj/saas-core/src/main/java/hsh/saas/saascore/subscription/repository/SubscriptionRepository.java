package hsh.saas.saascore.subscription.repository;

import hsh.saas.saascore.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Subscription 영속화 저장소.
 *
 * <p>Spring Data JPA 규칙 기반 메서드명을 사용해
 * 테넌트 단위 조회를 간단히 표현한다.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /**
     * 특정 테넌트가 보유한 모든 구독을 조회한다.
     *
     * <p>메서드명 파생 쿼리로 {@code tenant_id = ?} 조건이 자동 생성된다.
     *
     * @param tenantId 테넌트 ID
     * @return 해당 테넌트의 구독 목록
     */
    List<Subscription> findByTenantId(Long tenantId);
}

