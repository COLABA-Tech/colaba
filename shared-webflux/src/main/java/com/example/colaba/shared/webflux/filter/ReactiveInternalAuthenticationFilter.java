package com.example.colaba.shared.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class ReactiveInternalAuthenticationFilter implements WebFilter {

    private final String internalApiKey;

    public ReactiveInternalAuthenticationFilter(String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("Processing request: {}", path);

        String key = exchange.getRequest().getHeaders().getFirst("X-Internal-Key");
        log.info("Internal key: {}", key);

        if (key == null || !key.equals(internalApiKey)) {
            log.warn("Invalid or missing internal key for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.info("Internal key validated for path: {}", path);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }
}
