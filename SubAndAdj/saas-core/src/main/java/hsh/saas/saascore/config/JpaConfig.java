package hsh.saas.saascore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 공통 설정.
 *
 * <p>{@code @CreatedDate}, {@code @LastModifiedDate} 같은 감사 필드를
 * 엔티티 저장/수정 시점에 자동으로 채우기 위해 auditing을 활성화한다.
 *
 * <p>core 모듈에 위치시켜, 도메인 엔티티가 API 모듈과 무관하게
 * 동일 감사 정책을 적용받도록 한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
