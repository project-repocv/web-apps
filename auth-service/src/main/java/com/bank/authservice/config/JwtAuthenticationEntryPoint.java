package com.bank.authservice.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.error("Unauthorized error: {}", authException.getMessage());
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String body = String.format(
            \"{\\\"timestamp\\\": \\\"%s\\\", \\\"status\\\": 401, \\\"error\\\": \\\"Unauthorized\\\", \\\"message\\\": \\\"Error: Unauthorized\\\", \\\"path\\\": \\\"%s\\\"}\",
            java.time.Instant.now().toString(), request.getRequestURI());
        response.getOutputStream().print(body);
    }
}
