package com.bank.apigateway.config;

import com.bank.apigateway.filter.AuthenticationFilter;
import com.bank.apigateway.filter.LoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {
    
    private final AuthenticationFilter authenticationFilter;
    private final LoggingFilter loggingFilter;
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("customer-service", r -> r.path("/api/customers/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .rewritePath("/api/(?<remaining>.*)", "/api/${remaining}")
                )
                .uri("lb://customer-service"))
                
            .route("account-service", r -> r.path("/api/accounts/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .rewritePath("/api/(?<remaining>.*)", "/api/${remaining}")
                )
                .uri("lb://account-service"))
                
            .route("transaction-service", r -> r.path("/api/transactions/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .rewritePath("/api/(?<remaining>.*)", "/api/${remaining}")
                )
                .uri("lb://transaction-service"))
                
            .route("auth-service", r -> r.path("/api/auth/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .rewritePath("/api/(?<remaining>.*)", "/api/${remaining}")
                )
                .uri("lb://auth-service"))
                
            .route("notification-service", r -> r.path("/api/notifications/**")
                .filters(f -> f
                    .filter(loggingFilter.apply(new LoggingFilter.Config()))
                    .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                    .rewritePath("/api/(?<remaining>.*)", "/api/${remaining}")
                )
                .uri("lb://notification-service"))
                
            .build();
    }
}
