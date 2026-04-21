package com.bank.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    
    public LoggingFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            var request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethodValue();
            String remoteAddress = request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
            
            log.info("Incoming request: {} {} from {}", method, path, remoteAddress);
            
            long startTime = System.currentTimeMillis();
            
            return chain.filter(exchange).doFinally(signalType -> {
                var response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                
                log.info("Outgoing response: {} {} took {}ms", 
                    response.getStatusCode(), path, duration);
            });
        };
    }
    
    public static class Config {
    }
}
