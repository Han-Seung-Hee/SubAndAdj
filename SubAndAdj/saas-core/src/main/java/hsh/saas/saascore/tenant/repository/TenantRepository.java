package hsh.saas.saascore.tenant.repository;

import hsh.saas.saascore.tenant.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 테넌트 영속화 저장소.
 *
 * <p>기본 CRUD는 {@link JpaRepository} 기본 메서드를 사용한다.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
}

