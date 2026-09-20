package com.dhami.ecom.service.impl;

import com.dhami.ecom.config.JwtProperties;
import com.dhami.ecom.dto.*;
import com.dhami.ecom.entity.PasswordResetTokenEntity;
import com.dhami.ecom.entity.RefreshTokenEntity;
import com.dhami.ecom.entity.RoleEntity;
import com.dhami.ecom.entity.UserEntity;
import com.dhami.ecom.enums.RoleName;
import com.dhami.ecom.enums.StatusEnum;
import com.dhami.ecom.repository.PasswordResetTokenRepository;
import com.dhami.ecom.repository.RoleEntityRepository;
import com.dhami.ecom.repository.UserEntityRepository;
import com.dhami.ecom.security.CookieService;
import com.dhami.ecom.security.JwtService;
import com.dhami.ecom.security.RefreshTokenService;
import com.dhami.ecom.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleEntityRepository roleEntityRepository;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private static final int RESET_TOKEN_TTL_MINUTES = 15;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed - email already exists: {}", request.email());
            throw new RuntimeException("Email already exists");
        }
        RoleEntity userRole = roleEntityRepository.findByName(RoleName.ROLE_CUSTOMER)
                        .orElseThrow();

        UserEntity userToRegister = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .status(StatusEnum.INACTIVE)
                .roles(Set.of(userRole))
                .build();

       userRepository.save(userToRegister);
        log.info("User registered successfully with email: {}", request.email());
        return new RegisterResponse(
                "User registered successfully"
        );
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login attempt for email: {}", loginRequest.email());

       UserEntity user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(()->{
                    log.warn("Login failed - user not found: {}", loginRequest.email());
                    return new RuntimeException("Invalid credentials");
                });

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user);
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = jwtService.generateRefreshToken(user, refreshToken.getJti());
        log.info("User logged in successfully: {}", loginRequest.email());
        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                jwtProperties.accessTtlSeconds(),
                jwtProperties.refreshTtlSeconds(),
                "Login Successful"
        );
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String incomingToken = request.refreshToken();
        log.info("Refresh token request received");

        if (!jwtService.isRefreshTokenSignatureValid(incomingToken)) {
            log.warn("Refresh failed - invalid token signature");
            throw new RuntimeException("Invalid refresh token signature");
        }
        // Extract claims from the verified token
        String jti   = jwtService.extractJti(incomingToken);
        String email = jwtService.extractUsername(incomingToken);

        // Load the user
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Rotate — validates DB state, detects reuse, revokes old, saves new
        //    All DB work lives inside RefreshTokenService, not here
        refreshTokenService.rotateRefreshToken(jti, user);

        // Return fresh token pair
        return null;
    }

    @Transactional
    public AuthMetadata refresh(HttpServletRequest request, HttpServletResponse response) {
        log.info("Cookie-based refresh token request received");

        String refreshToken = cookieService.extractRefreshToken(request)
                .orElseThrow(() -> {
                    log.warn("Refresh failed - no refresh cookie found");
                    return new RuntimeException("Refresh cookie missing");
                });

        if (!jwtService.isRefreshTokenSignatureValid(refreshToken)) {
            log.warn("Refresh failed - invalid refresh token signature");
            throw new RuntimeException("Invalid refresh token");
        }

        String jti   = jwtService.extractJti(refreshToken);
        String email = jwtService.extractUsername(refreshToken);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        refreshTokenService.rotateRefreshToken(jti, user);
        issueTokenPair(user, response);

        return new AuthMetadata(user.getEmail(), jwtProperties.accessTtlSeconds());
    }

//    @Transactional
//    public void logout(RefreshRequest request) {
//        // Best-effort — invalid tokens just result in a no-op, not an error
//        if (!jwtService.isRefreshTokenSignatureValid(request.refreshToken())) {
//            return; // already invalid, nothing to revoke
//        }
//
//        String email = jwtService.extractUsername(request.refreshToken());
//        userRepository.findByEmail(email)
//                .ifPresent(user -> refreshTokenService.revokeAllForUser(user.getId()));
//    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout request received");
        cookieService.extractRefreshToken(request).ifPresent(token -> {
            if (jwtService.isRefreshTokenSignatureValid(token)) {
                String email = jwtService.extractUsername(token);
                userRepository.findByEmail(email)
                        .ifPresent(user -> {
                            refreshTokenService.revokeAllForUser(user.getId());
                            log.info("All refresh tokens revoked for user: {}", email);
                        });
            }
        });
        cookieService.clearAuthCookies(response);
        log.info("Auth cookies cleared");
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Password change request for email: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            log.warn("Password change failed - incorrect current password for: {}", email);
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        refreshTokenService.revokeAllForUser(user.getId());
        log.info("Password changed successfully for email: {}", email);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request received for email: {}", request.email());
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.revokeAllForUser(user.getId());

            String rawToken   = generateSecureToken();
            String hashedToken = passwordEncoder.encode(rawToken);

            PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                    .token(hashedToken)
                    .user(user)
                    .expiresAt(Instant.now().plusSeconds(RESET_TOKEN_TTL_MINUTES * 60L))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            log.info("Password reset token generated for user: {}", request.email());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Password reset request received");

        PasswordResetTokenEntity resetToken = passwordResetTokenRepository
                .findValidTokenForUser(request.token())
                .filter(t -> passwordEncoder.matches(request.token(), t.getToken()))
                .orElseThrow(() -> {
                    log.warn("Password reset failed - invalid or expired token");
                    return new RuntimeException("Invalid or expired reset token");
                });

        if (resetToken.isExpired()) {
            log.warn("Password reset failed - token has expired");
            throw new RuntimeException("Reset token has expired");
        }

        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenService.revokeAllForUser(user.getId());
        log.info("Password reset successful for email: {}", user.getEmail());
    }

    private void issueTokenPair(UserEntity user, HttpServletResponse response) {
        RefreshTokenEntity stored = refreshTokenService.createRefreshToken(user);
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, stored.getJti());
        cookieService.addAccessTokenCookie(response, accessToken);
        cookieService.addRefreshTokenCookie(response, refreshToken);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
