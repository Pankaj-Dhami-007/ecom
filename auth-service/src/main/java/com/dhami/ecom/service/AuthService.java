package com.dhami.ecom.service;

import com.dhami.ecom.dto.LoginRequest;
import com.dhami.ecom.dto.AuthResponse;
import com.dhami.ecom.dto.RegisterRequest;
import com.dhami.ecom.dto.RegisterResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
}
