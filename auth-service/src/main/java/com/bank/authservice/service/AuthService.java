package com.bank.authservice.service;

import com.bank.authservice.dto.*;
import com.bank.authservice.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    
    public JwtResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userService.findByUsername(request.getUsername());
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new IllegalStateException("User account is not active");
            }
            List<String> roles = user.getRoles().stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).toList();
            String accessToken = jwtService.generateToken(user.getUsername(), roles);
            var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
            log.info("User authenticated successfully: {}", request.getUsername());
            return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken.getToken())
                    .expiresIn(jwtService.getJwtExpiration()).customerId(user.getCustomerId()).roles(roles).build();
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new RuntimeException("Invalid credentials");
        }
    }
    
    public JwtResponse refreshToken(TokenRefreshRequest request) {
        var refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = userService.findByUsername(refreshToken.getUsername());
        if (user == null || user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User not found or inactive");
        }
        List<String> roles = user.getRoles().stream().map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).toList();
        String newAccessToken = jwtService.generateToken(user.getUsername(), roles);
        var newRefreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        log.info("Refreshed token for user: {}", user.getUsername());
        return JwtResponse.builder().accessToken(newAccessToken).refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration()).customerId(user.getCustomerId()).roles(roles).build();
    }
    
    public JwtResponse register(RegisterRequest request) {
        User user = userService.createUser(request);
        user = userService.activateUser(user.getId());
        List<String> roles = user.getRoles().stream().map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).toList();
        String accessToken = jwtService.generateToken(user.getUsername(), roles);
        var refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        log.info("Registered and authenticated user: {}", request.getUsername());
        return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken.getToken())
                .expiresIn(jwtService.getJwtExpiration()).customerId(user.getCustomerId()).roles(roles).build();
    }
    
    public void logout(String username) {
        refreshTokenService.deleteByUsername(username);
        log.info("Logged out user: {}", username);
    }
}
