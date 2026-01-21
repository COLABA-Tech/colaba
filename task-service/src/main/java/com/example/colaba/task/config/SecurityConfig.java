package com.example.colaba.task.config;

import com.example.colaba.shared.common.dto.common.ErrorResponseDto;
import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webmvc.filter.InternalAuthenticationFilter;
import com.example.colaba.shared.webmvc.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ObjectMapper objectMapper;

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
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
    public SecurityFilterChain internalSecurityFilterChain(
            HttpSecurity http,
            @Value("${internal.api-key}") String internalApiKey) throws Exception {

        InternalAuthenticationFilter internalFilter =
                new InternalAuthenticationFilter(internalApiKey);

        http
                .securityMatcher("/api/tasks/internal/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .addFilterBefore(internalFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Unauthorized")
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .message("Not authenticated")
                                    .path(request.getRequestURI())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            try {
                                String json = objectMapper.writeValueAsString(dto);
                                response.getWriter().write(json);
                                response.getWriter().flush();
                            } catch (Exception e) {
                                log.error("Failed to write 401 JSON", e);
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("{\"error\":\"Internal server error\"}");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Forbidden")
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("No permission")
                                    .path(request.getRequestURI())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            try {
                                String json = objectMapper.writeValueAsString(dto);
                                response.getWriter().write(json);
                                response.getWriter().flush();
                            } catch (Exception e) {
                                log.error("Failed to write 403 JSON", e);
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("{\"error\":\"Internal server error\"}");
                            }
                        })
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain mainSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/health",
                                "/v3/api-docs/**",
                                "/v3/api-docs-task",
                                "/v3/api-docs-task/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger-config",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/tasks/**", "/api/comments/**", "/api/task-tags/**").authenticated()
                        .anyRequest().denyAll()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Unauthorized")
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .message("Not authenticated")
                                    .path(request.getRequestURI())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            try {
                                String json = objectMapper.writeValueAsString(dto);
                                response.getWriter().write(json);
                                response.getWriter().flush();
                            } catch (Exception e) {
                                log.error("Failed to write 401 JSON", e);
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("{\"error\":\"Internal server error\"}");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ErrorResponseDto dto = ErrorResponseDto.builder()
                                    .error("Forbidden")
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .message("No permission")
                                    .path(request.getRequestURI())
                                    .timestamp(OffsetDateTime.now())
                                    .build();

                            try {
                                String json = objectMapper.writeValueAsString(dto);
                                response.getWriter().write(json);
                                response.getWriter().flush();
                            } catch (Exception e) {
                                log.error("Failed to write 403 JSON", e);
                                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                response.getWriter().write("{\"error\":\"Internal server error\"}");
                            }
                        })
                );

        return http.build();
    }
}