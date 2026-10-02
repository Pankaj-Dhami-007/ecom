package com.dhami.authservice.service;

import com.dhami.authservice.dto.LoginRequest;
import com.dhami.authservice.dto.AuthResponse;
import com.dhami.authservice.dto.RegisterRequest;
import com.dhami.authservice.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
}
