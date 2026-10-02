package com.dhami.authservice.security;

import com.dhami.authservice.config.JwtProperties;
import com.dhami.authservice.entity.RefreshTokenEntity;
import com.dhami.authservice.entity.UserEntity;
import com.dhami.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user) {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .jti(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTtlSeconds()))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the stored token and rotates it — old one is revoked,
     * new one is issued. This is atomic within the transaction.
     *
     * If a revoked token is reused, ALL tokens for that user are revoked
     * (signals token theft / replay attack).
     */
    @Transactional
    public RefreshTokenEntity rotateRefreshToken(String jti, UserEntity user) {
        RefreshTokenEntity existing = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (existing.isRevoked()) {
            // Possible token theft — revoke the entire family
            log.warn("Reuse of revoked refresh token detected for user {}. Revoking all tokens.", user.getEmail());
            refreshTokenRepository.revokeAllByUserId(user.getId());
            throw new RuntimeException("Refresh token already used — possible token theft detected");
        }

        if (existing.isExpired()) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Issue new token
        RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                .jti(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);

        // Revoke old token and record what replaced it
        existing.setRevoked(true);
        existing.setReplacedByJti(newToken.getJti());
        refreshTokenRepository.save(existing);

        return newToken;
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        int count = refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Revoked {} refresh token(s) for userId {}", count, userId);
    }

    // Run nightly to clean up expired tokens from the DB
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Purged {} expired refresh tokens", deleted);
    }
}

/*
// Inside the refresh token's payload (decoded):
{
  "jti": "a3f7c2d1-84bb-4e21-9012-abc123def456",  ← unique ID for THIS token
  "sub": "user@example.com",
  "typ": "refresh",
  "iat": 1717000000,
  "exp": 1717604800
}

 */