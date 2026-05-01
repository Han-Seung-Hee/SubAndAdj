package hsh.saas.saasapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 애플리케이션 진입점.
 *
 * <p>실행 모듈은 saas-api지만, 엔티티/리포지토리는 saas-core에 있으므로
 * 스캔 범위를 명시적으로 확장해 멀티모듈 빈/엔티티를 모두 로딩한다.
 */
@SpringBootApplication(scanBasePackages = "hsh.saas") // [핵심] hsh.saas 하위의 모든 패키지를 스캔합니다.
@EntityScan(basePackages = "hsh.saas.saascore") // 엔티티 스캔 경로 설정
@EnableJpaRepositories(basePackages = "hsh.saas.saascore") // JPA 리포지토리 스캔 경로 설정
public class SaasApiApplication {

    /** Spring Boot 런처. */
    public static void main(String[] args) {
        SpringApplication.run(SaasApiApplication.class, args);
    }
}
