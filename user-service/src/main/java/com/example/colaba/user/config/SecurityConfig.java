package com.example.colaba.user.config;

import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webflux.filter.ReactiveInternalAuthenticationFilter;
import com.example.colaba.shared.webflux.filter.ReactiveJwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
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
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, JwtService jwtService,
                                                      @Value("${internal.api-key}") String internalApiKey) {
        ReactiveInternalAuthenticationFilter internalFilter = new ReactiveInternalAuthenticationFilter(internalApiKey);
        ReactiveJwtAuthenticationFilter jwtFilter = new ReactiveJwtAuthenticationFilter(jwtService);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)

                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .addFilterBefore(internalFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> auth
                                .pathMatchers(
                                        "/actuator/**",
                                        "/health",
                                        "/v3/api-docs**",
                                        "/swagger-ui**"
                                ).permitAll()

                                .pathMatchers("/api/users/internal/**").access((authentication, context) -> {
                                    String key = context.getExchange().getRequest().getHeaders().getFirst("X-Internal-Key");
                                    boolean granted = key != null && key.equals(internalApiKey);
                                    return Mono.just(new AuthorizationDecision(granted));
                                })

                                .pathMatchers("/api/users/**").authenticated()
                                .anyExchange().denyAll()
                );
        return http.build();
    }
}