package com.dhami.cartservice.common.filter;

import com.dhami.cartservice.common.context.RequestUserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class UserContextFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            Long userId = parseUserId(request.getHeader(USER_ID_HEADER));
            if (userId != null) {
                RequestUserContext.setUserId(userId);
            }
            filterChain.doFilter(request, response);
        } finally {
            RequestUserContext.clear();
        }
    }

    private Long parseUserId(String header) {
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
