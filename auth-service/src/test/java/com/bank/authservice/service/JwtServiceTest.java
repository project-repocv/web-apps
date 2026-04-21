package com.bank.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        secret = "mySecretKeyForBankingApplicationThatShouldBeLongAndSecureAndRandomlyGeneratedForProductionUse";
        signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        
        jwtService = new JwtService();
        
        try {
            java.lang.reflect.Field field = JwtService.class.getDeclaredField("jwtSecret");
            field.setAccessible(true);
            field.set(jwtService, secret);
            
            java.lang.reflect.Field expField = JwtService.class.getDeclaredField("jwtExpiration");
            expField.setAccessible(true);
            expField.setLong(jwtService, 3600000L);
            
            java.lang.reflect.Field refreshExpField = JwtService.class.getDeclaredField("refreshExpiration");
            refreshExpField.setAccessible(true);
            refreshExpField.setLong(jwtService, 2592000000L);
        } catch (Exception e) {
            fail("Could not set test fields: " + e.getMessage());
        }
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpiration() {
        Date expiration = new Date(System.currentTimeMillis() + 3600000);
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Date extractedExpiration = jwtService.extractExpiration(token);

        assertNotNull(extractedExpiration);
        assertTrue(Math.abs(expiration.getTime() - extractedExpiration.getTime()) < 1000);
    }

    @Test
    void generateToken_ValidInput_ReturnsToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtService.generateToken(username, roles);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals(username, claims.getSubject());
        assertTrue(claims.containsKey("roles"));
    }

    @Test
    void generateRefreshToken_ValidInput_ReturnsToken() {
        String username = "testuser";

        String token = jwtService.generateRefreshToken(username);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertEquals(username, claims.getSubject());
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Boolean isValid = jwtService.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        String username = "testuser";
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
                .setExpiration(new Date(System.currentTimeMillis() - 1800000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Boolean isValid = jwtService.validateToken(token);

        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
                .setExpiration(new Date(System.currentTimeMillis() - 1800000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Boolean isExpired = jwtService.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void isTokenExpired_ValidToken_ReturnsFalse() {
        String token = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Boolean isExpired = jwtService.isTokenExpired(token);

        assertFalse(isExpired);
    }
}
