package com.dhami.ecom.controller;

import com.dhami.ecom.common.dto.ApiResponse;
import com.dhami.ecom.constants.Routes;
import com.dhami.ecom.dto.LoginRequest;
import com.dhami.ecom.dto.AuthResponse;
import com.dhami.ecom.dto.RegisterRequest;
import com.dhami.ecom.dto.RegisterResponse;
import com.dhami.ecom.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.Auth.ROOT)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping(Routes.Auth.REGISTER)
    public ResponseEntity<ApiResponse<RegisterResponse>> registerUser(
            @RequestBody RegisterRequest request
            ){
        log.info("Register request received for email: {}", request.email());
        RegisterResponse registerResponse = authService.register(request);

        log.info("User registered successfully with email: {}", request.email());
        return new ResponseEntity<>(
                ApiResponse.success(registerResponse),
                HttpStatus.CREATED
        );
    }

    @PostMapping(Routes.Auth.LOGIN)
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
            ){
        log.info("Login request received for email: {}", request.email());
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, response)));
    }
}
