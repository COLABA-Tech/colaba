package com.example.colaba.auth.unit;

import com.example.colaba.auth.dto.AuthResponse;
import com.example.colaba.auth.dto.LoginRequest;
import com.example.colaba.auth.dto.RegisterRequest;
import com.example.colaba.auth.service.AuthService;
import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.shared.webmvc.circuit.UserServiceClientWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserServiceClientWrapper userServiceClient;

    @InjectMocks
    private AuthService authService;

    private final Long adminUserId = 1L;
    private final Long regularUserId = 2L;
    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String encodedPassword = "encodedPassword123";
    private final String token = "jwt.token.here";

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserAuthDto userAuthDto;
    private UserResponse userResponse;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                username,
                email,
                password,
                UserRole.USER
        );

        loginRequest = new LoginRequest(username, password);

        userAuthDto = new UserAuthDto(
                1L,
                username,
                email,
                encodedPassword,
                "USER"
        );

        userResponse = new UserResponse(
                1L,
                username,
                email,
                "USER"
        );

        userDetails = new User(
                username,
                encodedPassword,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void register_adminUser_success() {
        // Given
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userServiceClient.createUser(any(UserAuthDto.class))).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userServiceClient.findForAuthByUsername(username)).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        AuthResponse result = authService.register(registerRequest, adminUserId);

        // Then
        assertNotNull(result);
        assertEquals(token, result.token());
        assertEquals(userResponse.id(), result.user().id());
        assertEquals(username, result.user().username());
        assertEquals(email, result.user().email());

        verify(passwordEncoder, times(2)).encode(password);
        verify(userServiceClient).createUser(argThat(dto ->
                dto.username().equals(username) &&
                        dto.email().equals(email) &&
                        dto.password().equals(encodedPassword) &&
                        dto.role().equals("USER")
        ));
    }

    @Test
    @Disabled
    void register_nonAdminUser_throwsAccessDenied() {
        // Given
        when(userServiceClient.isAdmin(regularUserId)).thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> authService.register(registerRequest, regularUserId));
        assertEquals("Only administrators can register new users", exception.getMessage());

        verify(userServiceClient).isAdmin(regularUserId);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userServiceClient, never()).createUser(any());
    }

    @Test
    void register_withEmailAsUsername_success() {
        // Given
        RegisterRequest emailRegisterRequest = new RegisterRequest(
                email,
                email,
                password,
                UserRole.USER
        );

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Исправлено
        when(userServiceClient.createUser(any(UserAuthDto.class))).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userServiceClient.findForAuthByEmail(email)).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        AuthResponse result = authService.register(emailRegisterRequest, adminUserId);

        // Then
        assertNotNull(result);
        verify(userServiceClient).findForAuthByEmail(email);
        verify(userServiceClient, never()).findForAuthByUsername(anyString());
    }

    @Test
    void register_withAdminRole_success() {
        // Given
        RegisterRequest adminRequest = new RegisterRequest(
                "adminuser",
                "admin@example.com",
                password,
                UserRole.ADMIN
        );

        UserAuthDto adminAuthDto = new UserAuthDto(
                2L,
                "adminuser",
                "admin@example.com",
                encodedPassword,
                "ADMIN"
        );

        UserDetails adminUserDetails = new User(
                "adminuser",
                encodedPassword,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Исправлено
        when(userServiceClient.createUser(any(UserAuthDto.class))).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(adminUserDetails);

        when(userServiceClient.findForAuthByUsername("adminuser")).thenReturn(adminAuthDto);
        when(jwtService.generateToken(anyLong(), eq("ADMIN"))).thenReturn(token);

        // When
        AuthResponse result = authService.register(adminRequest, adminUserId);

        // Then
        assertNotNull(result);
        verify(jwtService).generateToken(anyLong(), eq("ADMIN"));
    }

    @Test
    void login_withUsername_success() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userServiceClient.findForAuthByUsername(username)).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals(token, result.token());
        assertEquals(userResponse.id(), result.user().id());
        assertEquals(username, result.user().username());

        verify(authenticationManager).authenticate(argThat(token ->
                token.getPrincipal().equals(username) &&
                        token.getCredentials().equals(password)
        ));
        verify(userServiceClient).findForAuthByUsername(username);
        verify(userServiceClient, never()).findForAuthByEmail(anyString());
        verify(jwtService).generateToken(userAuthDto.id(), "USER");
    }

    @Test
    void login_withEmail_success() {
        // Given
        LoginRequest emailLoginRequest = new LoginRequest(email, password);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userServiceClient.findForAuthByEmail(email)).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        AuthResponse result = authService.login(emailLoginRequest);

        // Then
        assertNotNull(result);
        verify(userServiceClient).findForAuthByEmail(email);
        verify(userServiceClient, never()).findForAuthByUsername(anyString());
    }

    @Test
    void login_authenticationFails_throwsException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {
                });

        // When & Then
        assertThrows(org.springframework.security.core.AuthenticationException.class,
                () -> authService.login(loginRequest));

        verify(userServiceClient, never()).findForAuthByUsername(anyString());
        verify(userServiceClient, never()).findForAuthByEmail(anyString());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void login_userServiceReturnsNull_throwsException() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userServiceClient.findForAuthByUsername(username)).thenReturn(null);

        // When & Then
        assertThrows(NullPointerException.class, // или другой ожидаемый exception
                () -> authService.login(loginRequest));
    }

    @Test
    void login_withDifferentRoles_generatesCorrectToken() {
        // Given
        UserAuthDto managerAuthDto = new UserAuthDto(
                3L,
                "manager",
                "manager@example.com",
                encodedPassword,
                "MANAGER"
        );

        UserDetails managerUserDetails = new User(
                "manager",
                encodedPassword,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        );

        LoginRequest managerLogin = new LoginRequest("manager", password);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(managerUserDetails);
        when(userServiceClient.findForAuthByUsername("manager")).thenReturn(managerAuthDto);
        when(jwtService.generateToken(anyLong(), eq("MANAGER"))).thenReturn(token);

        // When
        AuthResponse result = authService.login(managerLogin);

        // Then
        assertNotNull(result);
        verify(jwtService).generateToken(managerAuthDto.id(), "MANAGER");
    }

    @Test
    void login_verifiesPasswordEncodingLog() {
        // Given
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userServiceClient.findForAuthByUsername(username)).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        authService.login(loginRequest);

        // Then - проверяем что passwordEncoder.encode вызывается в логе
        verify(passwordEncoder).encode(password);
    }

    @Test
    void register_withSpecialCharactersInUsername_success() {
        // Given
        RegisterRequest specialRequest = new RegisterRequest(
                "user.name_123",
                "user.name@example.com",
                password,
                UserRole.USER
        );

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        // Исправлено
        when(userServiceClient.createUser(any(UserAuthDto.class))).thenReturn(null);

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        when(userServiceClient.findForAuthByUsername("user.name_123")).thenReturn(userAuthDto);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn(token);

        // When
        AuthResponse result = authService.register(specialRequest, adminUserId);

        // Then
        assertNotNull(result);
        verify(userServiceClient).createUser(argThat(dto ->
                dto.username().equals("user.name_123")
        ));
    }
}