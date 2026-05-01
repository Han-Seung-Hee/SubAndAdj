package hsh.saas.saasapi.tenant.controller;

import hsh.saas.saascore.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 테넌트 생성 API 어댑터.
 *
 * <p>컨트롤러는 HTTP 요청 파싱/응답 생성만 수행하고,
 * 실제 비즈니스 처리는 {@link TenantService}로 위임한다.
 */
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    /** 코어 유스케이스 서비스. */
    private final TenantService tenantService;

    /**
     * 고객사 생성 API
     * POST http://localhost:18080/api/v1/tenants?name=삼성
     *
     * @param name 생성할 고객사 이름
     * @return 생성 결과 메시지(현재는 단순 문자열)
     */
    @PostMapping
    public String create(@RequestParam String name) {
        Long id = tenantService.createTenant(name);
        return "고객사 등록 성공! ID: " + id;
    }
}

