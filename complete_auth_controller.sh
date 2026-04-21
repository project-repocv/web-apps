#!/bin/bash

# Controller: AuthController
cat > /workspace/auth-service/src/main/java/com/bank/authservice/controller/AuthController.java << 'EOF'
package com.bank.authservice.controller;

import com.bank.authservice.dto.*;
import com.bank.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());
        JwtResponse response = authService.authenticate(loginRequest);
        log.info("User authenticated successfully: {}", loginRequest.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getUsername());
        JwtResponse response = authService.register(registerRequest);
        log.info("User registered successfully: {}", registerRequest.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        log.info("Token refresh requested");
        JwtResponse response = authService.refreshToken(request);
        log.info("Token refreshed successfully");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            log.info("Logout requested for user: {}", username);
            authService.logout(username);
            log.info("User logged out successfully: {}", username);
        }
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Logged out successfully").build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            var authorities = authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList();
            var response = ApiResponse.builder().success(true).message("Current user info retrieved")
                    .data(java.util.Map.of("username", username, "roles", authorities)).build();
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).body(ApiResponse.builder().success(false).message("Not authenticated").build());
    }
}
EOF

echo "Auth Service controller created successfully"
