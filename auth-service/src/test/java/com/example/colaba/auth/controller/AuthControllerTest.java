package com.example.colaba.auth.controller;

import com.example.colaba.auth.dto.AuthResponse;
import com.example.colaba.auth.dto.LoginRequest;
import com.example.colaba.auth.dto.RegisterRequest;
import com.example.colaba.auth.service.AuthService;
import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webmvc.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "internal.api-key=supersecretbase64valuehereatleast32byteslong==",
        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
        "jwt.expiration=3600000",
        "jwt.issuer=colaba"
})
@Import(AuthControllerTest.TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        adminToken = "admin-token";
        userToken = "user-token";

        // Мокируем jwtService.validateToken
        when(jwtService.validateToken("admin-token")).thenReturn(
                new DefaultClaims(Map.of("sub", "1", "role", "ADMIN"))
        );
        when(jwtService.validateToken("user-token")).thenReturn(
                new DefaultClaims(Map.of("sub", "2", "role", "USER"))
        );
    }

    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("user", "User1234");
        AuthResponse response = new AuthResponse(
                "jwt-token",
                new UserResponse(1L, "user", "user@example.com", "USER")
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("user"));
    }

    @Test
    void register_success_asAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@example.com", "Newuser1234", UserRole.USER);
        AuthResponse response = new AuthResponse(
                "jwt-token",
                new UserResponse(2L, "newuser", "newuser@example.com", "USER")
        );

        when(authService.register(any(RegisterRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("newuser"));
    }

    @Test
    void register_forbidden_forNonAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@example.com", "Newuser1234", UserRole.USER);

        when(authService.register(any(RegisterRequest.class), eq(2L)))
                .thenThrow(new AccessDeniedException("Only administrators can register new users"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    static class TestSecurityConfig {

        @Autowired
        private JwtService jwtService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
    }
}
