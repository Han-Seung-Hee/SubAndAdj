package hsh.saas.saascore.tenant.service;

import hsh.saas.saascore.tenant.domain.Tenant;
import hsh.saas.saascore.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 테넌트 유스케이스 서비스.
 *
 * <p>도메인 규칙을 조합해 테넌트 생성/조회 시나리오를 제공한다.
 * 클래스 기본 트랜잭션은 읽기 전용이며, 쓰기 메서드만 별도로 override한다.
 */
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 모든 메소드에 트랜잭션 적용, 읽기 전용으로 설정
public class TenantService {
    /** 테넌트 저장소 포트의 JPA 구현체. */
    private final TenantRepository tenantRepository;

    /**
     * 고객사 등록
     *
     * <p>고유 이름 정책은 DB 유니크 제약으로 강제한다.
     *
     * @param name 고객사 명
     * @return 생성된 테넌트 ID
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
     *
     * @return 모든 고객사 목록
     */
    public List<Tenant> findAllTenants() {
        return tenantRepository.findAll();
    }
}

