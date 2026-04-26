package hsh.saas.saasapi.common.security.jwt;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Refresh Token 영속 저장소.
 *
 * <p>핵심은 "동일 refresh 토큰 동시 요청 시 1건만 성공" 보장이다.
 * 이를 위해 jti 조회 시 비관적 락(PESSIMISTIC_WRITE)을 사용한다.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    /**
     * 회전 대상 refresh 토큰을 행 락으로 조회한다.
     *
     * <p>동시 요청이 들어와도 첫 트랜잭션이 상태를 변경할 때까지
     * 후속 트랜잭션은 대기하므로 1회성 소비 정책을 구현할 수 있다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.jti = :jti")
    Optional<RefreshTokenEntity> findByJtiForUpdate(@Param("jti") String jti);

    /**
     * 동일 family 체인의 ACTIVE 토큰을 일괄 폐기한다.
     *
     * <p>재사용 공격(replay) 탐지 시 계보 단위로 차단 범위를 넓혀
     * 추가 오남용을 빠르게 막기 위한 쿼리다.
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