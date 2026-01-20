package com.example.colaba.auth.unit;

import com.example.colaba.auth.service.AuthUserDetailsService;
import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.webmvc.client.UserServiceClient;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    private final String username = "testuser";
    private final String email = "test@example.com";
    private final String password = "encodedPassword123";

    private UserAuthDto userAuthDto;

    @BeforeEach
    void setUp() {
        userAuthDto = new UserAuthDto(
                1L,
                username,
                email,
                password,
                "USER"
        );
    }

    @Test
    void loadUserByUsername_withUsername_success() {
        // Given
        when(userServiceClient.findForAuthByUsername(username)).thenReturn(userAuthDto);

        // When
        UserDetails userDetails = authUserDetailsService.loadUserByUsername(username);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));

        verify(userServiceClient).findForAuthByUsername(username);
        verify(userServiceClient, never()).findForAuthByEmail(anyString());
    }

    @Test
    void loadUserByUsername_withEmail_success() {
        // Given
        when(userServiceClient.findForAuthByEmail(email)).thenReturn(userAuthDto);

        // When
        UserDetails userDetails = authUserDetailsService.loadUserByUsername(email);

        // Then
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        verify(userServiceClient).findForAuthByEmail(email);
        verify(userServiceClient, never()).findForAuthByUsername(anyString());
    }

    @Test
    void loadUserByUsername_userNotFoundByUsername_throwsException() {
        // Given
        when(userServiceClient.findForAuthByUsername(username))
                .thenThrow(new FeignException.NotFound("Not found", mock(Request.class), null, null));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(username));
        assertTrue(exception.getMessage().contains("User not found: LOGIN " + username));
    }

    @Test
    void loadUserByUsername_userNotFoundByEmail_throwsException() {
        // Given
        when(userServiceClient.findForAuthByEmail(email))
                .thenThrow(new FeignException.NotFound("Not found", mock(Request.class), null, null));

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(email));
        assertTrue(exception.getMessage().contains("User not found: LOGIN " + email));
    }

    @Test
    void loadUserByUsername_serviceReturnsNull_throwsException() {
        // Given
        when(userServiceClient.findForAuthByUsername(username)).thenReturn(null);

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(username));
        assertTrue(exception.getMessage().contains("User not found: LOGIN " + username));
    }

    @Test
    void loadUserByUsername_feignExceptionOtherThanNotFound_throwsExceptionWithCause() {
        // Given
        FeignException.ServiceUnavailable serviceException =
                new FeignException.ServiceUnavailable("Service unavailable", mock(Request.class), null, null);
        when(userServiceClient.findForAuthByUsername(username))
                .thenThrow(serviceException);

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(username));
        assertTrue(exception.getMessage().contains("Error loading user: LOGIN " + username));
        assertEquals(serviceException, exception.getCause());
    }

    @Test
    void loadUserByUsername_withDifferentRoles_success() {
        // Given
        UserAuthDto adminAuthDto = new UserAuthDto(
                2L,
                "admin",
                "admin@example.com",
                password,
                "ADMIN"
        );
        when(userServiceClient.findForAuthByUsername("admin")).thenReturn(adminAuthDto);

        // When
        UserDetails userDetails = authUserDetailsService.loadUserByUsername("admin");

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_withManagerRole_success() {
        // Given
        UserAuthDto managerAuthDto = new UserAuthDto(
                3L,
                "manager",
                "manager@example.com",
                password,
                "MANAGER"
        );
        when(userServiceClient.findForAuthByUsername("manager")).thenReturn(managerAuthDto);

        // When
        UserDetails userDetails = authUserDetailsService.loadUserByUsername("manager");

        // Then
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")));
    }

    @Test
    void loadUserByUsername_emailWithPlusAddress_success() {
        // Given
        String emailWithPlus = "test+tag@example.com";
        UserAuthDto userWithPlusEmail = new UserAuthDto(
                4L,
                "testuser",
                emailWithPlus,
                password,
                "USER"
        );
        when(userServiceClient.findForAuthByEmail(emailWithPlus)).thenReturn(userWithPlusEmail);

        // When
        UserDetails userDetails = authUserDetailsService.loadUserByUsername(emailWithPlus);

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_emptyUsername_throwsException() {
        // Given
        String emptyUsername = "";

        // When & Then
        UsernameNotFoundException _ = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(emptyUsername));

        // Пустая строка не содержит @ → идёт в findForAuthByUsername
        verify(userServiceClient).findForAuthByUsername(emptyUsername);
    }

    @Test
    void loadUserByUsername_nullUsername_throwsException() {
        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername(null));

        assertTrue(exception.getMessage().contains("Error loading user: LOGIN null"));
        assertNotNull(exception.getCause());
        assertInstanceOf(NullPointerException.class, exception.getCause());
    }
}