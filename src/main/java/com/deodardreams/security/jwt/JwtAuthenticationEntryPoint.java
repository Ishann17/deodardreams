package com.deodardreams.security.jwt;

/**
 * Runs whenever an unauthenticated request is rejected at the filter-chain
 * level (missing, malformed, or expired bearer token) — BEFORE the request
 * ever reaches a controller. Without this, Spring Security falls back to a
 * plain, unstyled default response. This produces the same JSON error shape
 * used everywhere else in the API (see GlobalExceptionHandler), so clients
 * get a consistent response regardless of WHERE in the request lifecycle
 * the rejection happened.
 */

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", "A valid bearer token is required to access this resource.");
        errorResponse.put("path", request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
