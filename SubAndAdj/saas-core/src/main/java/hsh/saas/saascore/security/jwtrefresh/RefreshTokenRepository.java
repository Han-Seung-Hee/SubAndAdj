package hsh.saas.saascore.security.jwtrefresh;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for refresh token state transitions.
 *
 * <p>회전 시나리오의 핵심은 "같은 jti를 동시에 소비하지 못하게" 하는 것이므로,
 * 일부 조회는 비관적 락을 사용한다.
 * replay 발생 시에는 family 단위 일괄 업데이트로 피해 범위를 즉시 차단한다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    /**
     * jti 단건을 배타 잠금으로 조회한다.
     *
     * <p>동일 토큰 동시 제출 경쟁 상황에서 한 트랜잭션만 먼저 상태를 전이하도록 보장한다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.jti = :jti")
    Optional<RefreshTokenEntity> findByJtiForUpdate(@Param("jti") String jti);

    /**
     * 같은 family의 ACTIVE 토큰을 일괄 폐기한다.
     * <p>재사용 탐지 시, 같은 family의 ACTIVE 토큰을 모두 REVOKED로 전이해 추가 피해를 방지한다.
     * @param familyId 폐기 대상 회전 계보 식별자
     * @param status 변경할 상태(일반적으로 REVOKED)
     * @param now 상태 변경 시각
     * @return 실제로 상태가 변경된 행 수
     */
    @Modifying
    @Query("""
           update RefreshTokenEntity t
              set t.status = :status,
                  t.lastUsedAt = :now
            where t.familyId = :familyId
              and t.status = 'ACTIVE'
           """)
    int revokeActiveByFamily(@Param("familyId") String familyId,
                             @Param("status") RefreshTokenStatus status,
                             @Param("now") Instant now);
}

