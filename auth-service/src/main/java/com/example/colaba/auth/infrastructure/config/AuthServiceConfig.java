package com.example.colaba.auth.infrastructure.config;

import com.example.colaba.auth.application.port.PasswordEncoderPort;
import com.example.colaba.auth.application.service.AuthService;
import com.example.colaba.shared.common.application.security.JwtTokenService;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthServiceConfig {

    @Bean
    public AuthService authService(
            JwtTokenService jwtTokenService,
            PasswordEncoderPort passwordEncoder,
            UserServicePort userServicePort
    ) {
        return new AuthService(
                jwtTokenService,
                passwordEncoder,
                userServicePort
        );
    }
}

