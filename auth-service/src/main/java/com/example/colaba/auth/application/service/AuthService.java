package com.example.colaba.auth.application.service;

import com.example.colaba.auth.application.dto.AuthResponse;
import com.example.colaba.auth.application.dto.LoginRequest;
import com.example.colaba.auth.application.dto.RegisterRequest;
import com.example.colaba.auth.application.port.PasswordEncoderPort;
import com.example.colaba.shared.common.application.dto.user.UserAuthDto;
import com.example.colaba.shared.common.application.dto.user.UserResponse;
import com.example.colaba.shared.common.application.security.JwtTokenService;
import com.example.colaba.shared.common.domain.exception.common.AccessDeniedException;
import com.example.colaba.shared.common.domain.exception.common.AuthenticationException;
import com.example.colaba.shared.common.domain.exception.user.UserNotFoundException;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenService jwtService;
    private final PasswordEncoderPort passwordEncoder;
    private final UserServicePort userServiceClient;

    public AuthResponse register(RegisterRequest request, Long currentUserId) {
        if (!userServiceClient.isAdmin(currentUserId)) {
            throw new AccessDeniedException("Required role: ADMIN");
        }
        String hashedPassword = passwordEncoder.encode(request.password());
        userServiceClient.createUser(
                new UserAuthDto(null, request.username(), request.email(),
                        hashedPassword, request.role().getValue())
        );
        return login(new LoginRequest(request.username(), request.password()));
    }

    public AuthResponse login(LoginRequest request) {
        UserAuthDto user;

        try {
            if (request.login().contains("@")) {
                user = userServiceClient.findForAuthByEmail(request.login());
            } else {
                user = userServiceClient.findForAuthByUsername(request.login());
            }
        } catch (UserNotFoundException ex) {
            throw new AuthenticationException("Invalid login or password");
        }

        if (user == null || !passwordEncoder.matches(request.password(), user.password())) {
            throw new AuthenticationException("Invalid login or password");
        }

        String token = jwtService.generateToken(user.id(), user.role());
        return new AuthResponse(
                token,
                new UserResponse(user.id(), user.username(), user.email(), user.role())
        );
    }
}