package com.deodardreams.security.jwt;
/**
 * Runs when a request is rejected at the filter-chain level because the
 * caller IS identified (even if only as Spring Security's default anonymous
 * identity) but lacks the required role — e.g. a request with no/invalid
 * token hitting an endpoint like /api/admin-users that needs SUPER_ADMIN.
 * This is the sibling of JwtAuthenticationEntryPoint: that one handles "no
 * identity at all", this one handles "an identity, but not enough permission" —
 * both at the SAME filter-chain level, before Spring MVC/@RestControllerAdvice
 * ever gets involved.
 */

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "ACCESS_DENIED");
        errorResponse.put("message", "You do not have permission to access this resource.");
        errorResponse.put("path", request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}