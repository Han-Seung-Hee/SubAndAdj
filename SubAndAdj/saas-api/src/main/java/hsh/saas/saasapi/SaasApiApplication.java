package hsh.saas.saasapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "hsh.saas") // [핵심] hsh.saas 하위의 모든 패키지를 스캔합니다.
@EntityScan(basePackages = "hsh.saas.saascore.domain") // 엔티티 스캔 경로 설정
@EnableJpaRepositories(basePackages = "hsh.saas.saascore.repository") // JPA 리포지토리 스캔 경로 설정
public class SaasApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasApiApplication.class, args);
    }

}
