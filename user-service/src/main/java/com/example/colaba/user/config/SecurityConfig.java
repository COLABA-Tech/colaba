package com.example.colaba.user.config;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webflux.filter.ReactiveInternalAuthenticationFilter;
import com.example.colaba.shared.webflux.filter.ReactiveJwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
    @Order(1)
    public SecurityWebFilterChain internalSecurityFilterChain(
            ServerHttpSecurity http,
            @Value("${internal.api-key}") String internalApiKey) {

        ReactiveInternalAuthenticationFilter internalFilter =
                new ReactiveInternalAuthenticationFilter(internalApiKey);

        http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/api/users/internal/**"))
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
                        .authenticationEntryPoint((exchange, authException) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Unauthorized")
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .message("Not authenticated")
                                    .path(exchange.getRequest().getPath().value())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(dto))
                                    .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                                    .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
                                    .onErrorResume(e -> {
                                        log.error("Failed to serialize 401 response", e);
                                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                        return exchange.getResponse().writeWith(Mono.just(
                                                exchange.getResponse().bufferFactory().wrap(
                                                        "{\"error\":\"Internal server error\"}".getBytes()
                                                )
                                        ));
                                    });
                        })
                        .accessDeniedHandler((exchange, deniedException) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Forbidden")
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("No permission")
                                    .path(exchange.getRequest().getPath().value())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(dto))
                                    .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                                    .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
                                    .onErrorResume(e -> {
                                        log.error("Failed to serialize 403 response", e);
                                        return exchange.getResponse().writeWith(Mono.just(
                                                exchange.getResponse().bufferFactory().wrap(
                                                        "{\"error\":\"Internal server error\"}".getBytes()
                                                )
                                        ));
                                    });
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
                                "/v3/api-docs/**",
                                "/v3/api-docs-user",
                                "/v3/api-docs-user/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger-config",
                                "/favicon.ico",
                                "/webjars/swagger-ui/**"
                        ).permitAll()
                        .pathMatchers("/api/users/internal/**").permitAll()
                        .pathMatchers("/api/users/**").authenticated()
                        .anyExchange().denyAll()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((exchange, authException) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Unauthorized")
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .message("Not authenticated")
                                    .path(exchange.getRequest().getPath().value())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(dto))
                                    .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                                    .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
                                    .onErrorResume(e -> {
                                        log.error("Failed to serialize 401 response", e);
                                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                        return exchange.getResponse().writeWith(Mono.just(
                                                exchange.getResponse().bufferFactory().wrap(
                                                        "{\"error\":\"Internal server error\"}".getBytes()
                                                )
                                        ));
                                    });
                        })
                        .accessDeniedHandler((exchange, deniedException) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Forbidden")
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("No permission")
                                    .path(exchange.getRequest().getPath().value())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(dto))
                                    .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                                    .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer)))
                                    .onErrorResume(e -> {
                                        log.error("Failed to serialize 403 response", e);
                                        return exchange.getResponse().writeWith(Mono.just(
                                                exchange.getResponse().bufferFactory().wrap(
                                                        "{\"error\":\"Internal server error\"}".getBytes()
                                                )
                                        ));
                                    });
                        })
                );

        return http.build();
    }
}