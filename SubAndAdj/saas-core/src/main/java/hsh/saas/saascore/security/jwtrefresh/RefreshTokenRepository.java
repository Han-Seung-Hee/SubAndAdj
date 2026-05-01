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
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t where t.jti = :jti")
    Optional<RefreshTokenEntity> findByJtiForUpdate(@Param("jti") String jti);

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

