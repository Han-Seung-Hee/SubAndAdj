package hsh.saas.saascore.repository;


import hsh.saas.saascore.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    /*
    * 특정 고객사의 모든 구독 정보를 찾는 쿼리 메소드
    * Spring Data JPA 가 메소드 이름을 분석하여
    * 'SELECT * FROM subscription WHERE tenant_id = ?'
    * 와 같은 쿼리를 자동으로 생성합니다.
    * @param tenantId 고객사 ID
    * @return 해당 고객사의 구독 리스트
    */
    List<Subscription> findByTenantId(Long tenantId);

}
