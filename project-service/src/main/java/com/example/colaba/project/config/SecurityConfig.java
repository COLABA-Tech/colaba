package com.example.colaba.project.config;

import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webflux.filter.ReactiveInternalAuthenticationFilter;
import com.example.colaba.shared.webflux.filter.ReactiveJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public ReactiveInternalAuthenticationFilter internalFilter(
            @Value("${internal.api-key}") String internalApiKey
    ) {
        return new ReactiveInternalAuthenticationFilter(internalApiKey);
    }

    @Bean
    public ReactiveJwtAuthenticationFilter jwtFilter(JwtService jwtService) {
        return new ReactiveJwtAuthenticationFilter(jwtService);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, ReactiveJwtAuthenticationFilter jwtFilter,
                                                      ReactiveInternalAuthenticationFilter internalFilter) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .addFilterAt(internalFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> auth
                                .pathMatchers("/actuator/**", "/health", "/v3/api-docs**", "/swagger-ui**").permitAll()
//                        .pathMatchers("/api/projects/internal/**", "/api/tags/internal/**").access((authentication, context) -> {
//                            String key = context.getExchange().getRequest().getHeaders().getFirst("X-Internal-Key");
//                            return Mono.just(new AuthorizationDecision(key != null && key.equals(internalApiKey)));
//                        })
                                .pathMatchers("/api/projects/internal/**", "/api/tags/internal/**").permitAll()
                                .pathMatchers("/api/projects/**", "/api/tags/**").authenticated()
                                .anyExchange().denyAll()
                );
        return http.build();
    }
}
