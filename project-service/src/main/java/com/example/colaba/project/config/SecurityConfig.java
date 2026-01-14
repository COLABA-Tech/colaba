package com.example.colaba.project.config;

import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webflux.filter.ReactiveInternalAuthenticationFilter;
import com.example.colaba.shared.webflux.filter.ReactiveJwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

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
    @Order(1)
    public SecurityWebFilterChain internalSecurityFilterChain(
            ServerHttpSecurity http,
            @Value("${internal.api-key}") String internalApiKey) {

        ReactiveInternalAuthenticationFilter internalFilter =
                new ReactiveInternalAuthenticationFilter(internalApiKey);

        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers(
                        "/api/projects/internal/**",
                        "/api/tags/internal/**"
                ))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .addFilterAt(internalFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> auth
                        .anyExchange().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, _) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, _) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain mainSecurityFilterChain(
            ServerHttpSecurity http,
            JwtService jwtService) {

        ReactiveJwtAuthenticationFilter jwtFilter = new ReactiveJwtAuthenticationFilter(jwtService);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)

                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/actuator/**",
                                "/health",
                                "/v3/api-docs**",
                                "/swagger-ui**"
                        ).permitAll()
                        .pathMatchers(
                                "/api/projects/**",
                                "/api/tags/**"
                        ).authenticated()
                        .anyExchange().denyAll()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, _) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, _) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        })
                );

        return http.build();
    }
}
