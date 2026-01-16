package com.example.colaba.auth.service;

import com.example.colaba.auth.dto.AuthResponse;
import com.example.colaba.auth.dto.LoginRequest;
import com.example.colaba.auth.dto.RegisterRequest;
import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webmvc.client.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       PasswordEncoder passwordEncoder, UserServiceClient userServiceClient) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String hashedPassword = passwordEncoder.encode(request.password());
        userServiceClient.createUser(
                new UserAuthDto(null, request.username(), request.email(),
                        hashedPassword, request.role().getValue())
        );
        return login(new LoginRequest(request.username(), request.password()));
    }

    public AuthResponse login(LoginRequest request) {
        log.info(passwordEncoder.encode(request.password()));
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        UserAuthDto userAuth;
        if (request.login().contains("@")) {
            userAuth = userServiceClient.findForAuthByEmail(request.login());
        } else {
            userAuth = userServiceClient.findForAuthByUsername(request.login());
        }
        String token = jwtService.generateToken(
                userAuth.id(),
                userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
        );
        return new AuthResponse(token, new UserResponse(
                userAuth.id(),
                userAuth.username(),
                userAuth.email(),
                userAuth.role()
        ));
    }
}
