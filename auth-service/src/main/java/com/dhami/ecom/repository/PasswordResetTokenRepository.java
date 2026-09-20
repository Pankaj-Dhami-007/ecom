package com.dhami.ecom.repository;

import com.dhami.ecom.entity.PasswordResetTokenEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    Optional<PasswordResetTokenEntity> findByToken(String token);

    // Invalidate any existing unused tokens before issuing a new one
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity p SET p.used = true WHERE p.user.id = :userId AND p.used = false")
    void revokeAllForUser(Long userId);

    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity p WHERE p.expiresAt < :now")
    void deleteExpired(Instant now);

    @Query("SELECT p FROM PasswordResetTokenEntity p WHERE p.token = :token AND p.used = false AND p.expiresAt > CURRENT_TIMESTAMP")
    Optional<PasswordResetTokenEntity> findValidTokenForUser(@Param("token") @NotBlank String token);
}