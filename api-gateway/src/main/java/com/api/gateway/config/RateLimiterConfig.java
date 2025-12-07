package com.api.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;


@Configuration
public class RateLimiterConfig {

    @Bean
    @Primary
    public RedisRateLimiter defaultRedisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }

    @Bean
    public RedisRateLimiter strictRedisRateLimiter() {
        return new RedisRateLimiter(5, 10, 1);
    }

    @Bean
    @Primary
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Try to get authenticated principal (if JWT is validated by gateway)
            return exchange.getPrincipal()
                    .map(principal -> "user:" + principal.getName())
                    .switchIfEmpty(Mono.defer(() -> {
                        // Fallback to IP address
                        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
                        if (forwardedFor != null && !forwardedFor.isBlank()) {
                            return Mono.just("ip:" + forwardedFor.split(",")[0].trim());
                        }
                        String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                : "unknown";
                        return Mono.just("ip:" + remoteAddr);
                    }));
        };
    }
}
