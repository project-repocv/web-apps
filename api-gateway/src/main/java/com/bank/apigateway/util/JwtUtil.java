package com.bank.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtUtil {
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    public String extractUsername(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            return null;
        }
    }
    
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object roles = claims.get("roles");
            if (roles instanceof List) {
                return (List<String>) roles;
            }
            return List.of();
        } catch (Exception e) {
            log.error("Error extracting roles from token", e);
            return List.of();
        }
    }
    
    public Date extractExpiration(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Error extracting expiration from token", e);
            return null;
        }
    }
    
    public Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }
    
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Error validating token", e);
            return false;
        }
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSignKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
