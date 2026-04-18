package hsh.saas.saasapi.controller;

import hsh.saas.saascore.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController // JSON 형태로 응답을 반환하는 컨트롤러임을 명시합니다.
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    /**
     * 고객사 생성 API
     * POST http://localhost:18080/api/v1/tenants?name=삼성
     */
    @PostMapping
    public String create(@RequestParam String name) {
        Long id = tenantService.createTenant(name);
        return "고객사 등록 성공! ID: " + id;
    }
}