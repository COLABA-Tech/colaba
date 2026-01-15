package com.example.colaba.shared.webflux.filter;

import com.example.colaba.shared.common.security.JwtService;
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
public class ReactiveJwtAuthenticationFilter implements WebFilter {

    private final JwtService jwtService;

    public ReactiveJwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.info("Processing request in JWT filter: {}", path);

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        log.info("Authorization header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.info("JWT token extracted: {}", token);
            return Mono.fromCallable(() -> jwtService.validateToken(token))
                    .map(claims -> {
                        String authority = "ROLE_" + claims.get("role", String.class);
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );
                        log.debug("Authentication set for user: {} with authorities: {}", claims.getSubject(), auth.getAuthorities());
                        return auth;
                    })
                    .flatMap(auth ->
                            chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                    .onErrorResume(ex -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        } else {
            log.warn("No valid Bearer token found");
        }

        return chain.filter(exchange);
    }
}