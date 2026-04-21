package com.bank.apigateway.filter;

import com.bank.apigateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    
    private final JwtUtil jwtUtil;
    
    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return onError(exchange, "Authorization header is missing or invalid", HttpStatus.UNAUTHORIZED);
            }
            
            String token = authHeader.substring(7);
            
            if (!jwtUtil.validateToken(token)) {
                log.warn("Invalid JWT token");
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
            
            String username = jwtUtil.extractUsername(token);
            java.util.List<String> roles = jwtUtil.extractRoles(token);
            
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Roles", String.join(",", roles))
                    .build();
            
            log.info("Authenticated user: {} with roles: {}", username, roles);
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }
    
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        
        String errorBody = String.format("""
            {
                "timestamp": "%s",
                "status": %d,
                "error": "%s",
                "message": "%s",
                "path": "%s"
            }
            """, Instant.now().toString(), httpStatus.value(), httpStatus.getReasonPhrase(), err, exchange.getRequest().getPath());
        
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        return response.writeWith(
            org.springframework.core.io.buffer.DataBufferUtils.read(
                java.nio.ByteBuffer.wrap(errorBody.getBytes()),
                exchange.getResponse().bufferFactory())
        );
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
    public static class Config {
    }
}
