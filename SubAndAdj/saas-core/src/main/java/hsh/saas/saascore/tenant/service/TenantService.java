package hsh.saas.saascore.tenant.service;

import hsh.saas.saascore.tenant.domain.Tenant;
import hsh.saas.saascore.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 모든 메소드에 트랜잭션 적용, 읽기 전용으로 설정
public class TenantService {
    private final TenantRepository tenantRepository;

    /**
     * 고객사 등록
     * @param name 고객사 명
     */
    @Transactional // 쓰기 작업이므로 트랜잭션 적용
    public Long createTenant(String name) {
        Tenant tenant = Tenant.builder()
                .name(name)
                .build();

        Tenant savedTenant = tenantRepository.save(tenant);
        return savedTenant.getId();
    }

    /**
     * 고객사 조회
     * @return 모든 고객사 목록
     */
    public List<Tenant> findAllTenants() {
        return tenantRepository.findAll();
    }
}

