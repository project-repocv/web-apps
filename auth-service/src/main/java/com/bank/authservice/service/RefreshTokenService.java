package com.bank.authservice.service;

import com.bank.authservice.entity.RefreshToken;
import com.bank.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    
    public RefreshToken createRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .build();
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user: {}", username);
        return savedToken;
    }
    
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.deleteById(refreshToken.getId());
            throw new RuntimeException("Refresh token expired");
        }
        return refreshToken;
    }
    
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
        log.info("Deleted refresh token: {}", token);
    }
    
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
        log.info("Deleted refresh tokens for user: {}", username);
    }
}
