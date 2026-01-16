package com.example.colaba.user.unit;

import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.security.UserAccessCheckerLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserAccessCheckerLocalTest {

    @Mock
    private UserRepository userRepository;

    private UserAccessCheckerLocal userAccessChecker;

    private final Long testUserId = 1L;
    private final Long testAdminId = 2L;
    private final Long testTargetUserId = 3L;

    @BeforeEach
    void setUp() {
        userAccessChecker = new UserAccessCheckerLocal(userRepository);
    }

    @Test
    void isAdminMono_whenUserIsAdmin_returnsTrue() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = userAccessChecker.isAdminMono(testAdminId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void isAdminMono_whenUserIsNotAdmin_returnsFalse() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        // When
        Mono<Boolean> result = userAccessChecker.isAdminMono(testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getUserRoleMono_returnsUserRole() {
        // Given
        when(userRepository.findRoleById(testUserId))
                .thenReturn(Mono.just(UserRole.USER));

        // When
        Mono<UserRole> result = userAccessChecker.getUserRoleMono(testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(UserRole.USER)
                .verifyComplete();

        verify(userRepository).findRoleById(testUserId);
    }

    @Test
    void requireAdminMono_whenUserIsAdmin_completesSuccessfully() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        Mono<Void> result = userAccessChecker.requireAdminMono(testAdminId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void requireAdminMono_whenUserIsNotAdmin_throwsAccessDeniedException() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        // When
        Mono<Void> result = userAccessChecker.requireAdminMono(testUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("Required user role: ADMIN"))
                .verify();

        verify(userRepository).existsByIdAndRole(testUserId, UserRole.ADMIN);
    }

    @Test
    void requireAdminMono_whenUserNotFound_throwsAccessDeniedException() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> result = userAccessChecker.requireAdminMono(testUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("User not found"))
                .verify();
    }

    @Test
    void canManageUserMono_whenManagingOwnAccount_returnsTrue() {
        // Given
        // No mocks needed as it's a simple equality check

        // When
        Mono<Boolean> result = userAccessChecker.canManageUserMono(testUserId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(userRepository, never()).existsByIdAndRole(anyLong(), any());
    }

    @Test
    void canManageUserMono_whenAdminManagingOtherAccount_returnsTrue() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        Mono<Boolean> result = userAccessChecker.canManageUserMono(testAdminId, testTargetUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void canManageUserMono_whenNonAdminManagingOtherAccount_returnsFalse() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        // When
        Mono<Boolean> result = userAccessChecker.canManageUserMono(testUserId, testTargetUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).existsByIdAndRole(testUserId, UserRole.ADMIN);
    }

    @Test
    void requireCanManageUserMono_whenManagingOwnAccount_completesSuccessfully() {
        // When
        Mono<Void> result = userAccessChecker.requireCanManageUserMono(testUserId, testUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(userRepository, never()).existsByIdAndRole(anyLong(), any());
    }

    @Test
    void requireCanManageUserMono_whenAdminManagingOtherAccount_completesSuccessfully() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        Mono<Void> result = userAccessChecker.requireCanManageUserMono(testAdminId, testTargetUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void requireCanManageUserMono_whenNonAdminManagingOtherAccount_throwsAccessDeniedException() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        // When
        Mono<Void> result = userAccessChecker.requireCanManageUserMono(testUserId, testTargetUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("You can only manage your own account or as ADMIN"))
                .verify();

        verify(userRepository).existsByIdAndRole(testUserId, UserRole.ADMIN);
    }

    @Test
    void isAdmin_whenUserIsAdmin_returnsTrue() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        boolean result = userAccessChecker.isAdmin(testAdminId);

        // Then
        assert result;
        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void getUserRole_returnsUserRole() {
        // Given
        when(userRepository.findRoleById(testUserId))
                .thenReturn(Mono.just(UserRole.USER));

        // When
        UserRole result = userAccessChecker.getUserRole(testUserId);

        // Then
        assert result == UserRole.USER;
        verify(userRepository).findRoleById(testUserId);
    }

    @Test
    void requireAdmin_whenUserIsAdmin_doesNotThrowException() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When & Then (no exception should be thrown)
        userAccessChecker.requireAdmin(testAdminId);

        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void requireAdmin_whenUserIsNotAdmin_throwsAccessDeniedException() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));
        when(userRepository.findRoleById(testUserId))
                .thenReturn(Mono.just(UserRole.USER));

        // When & Then
        try {
            userAccessChecker.requireAdmin(testUserId);
            assert false; // Should not reach here
        } catch (AccessDeniedException e) {
            assert e.getMessage().equals("Required user role: ADMIN. Current role: USER");
        }

        verify(userRepository).existsByIdAndRole(testUserId, UserRole.ADMIN);
        verify(userRepository).findRoleById(testUserId);
    }

    @Test
    void canManageUser_whenManagingOwnAccount_returnsTrue() {
        // When
        boolean result = userAccessChecker.canManageUser(testUserId, testUserId);

        // Then
        assert result;
    }

    @Test
    void canManageUser_whenAdminManagingOtherAccount_returnsTrue() {
        // Given
        when(userRepository.existsByIdAndRole(testAdminId, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        // When
        boolean result = userAccessChecker.canManageUser(testAdminId, testTargetUserId);

        // Then
        assert result;
        verify(userRepository).existsByIdAndRole(testAdminId, UserRole.ADMIN);
    }

    @Test
    void requireCanManageUser_whenManagingOwnAccount_doesNotThrowException() {
        // When & Then (no exception should be thrown)
        userAccessChecker.requireCanManageUser(testUserId, testUserId);
    }

    @Test
    void requireCanManageUser_whenNonAdminManagingOtherAccount_throwsAccessDeniedException() {
        // Given
        when(userRepository.existsByIdAndRole(testUserId, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        // When & Then
        try {
            userAccessChecker.requireCanManageUser(testUserId, testTargetUserId);
            assert false; // Should not reach here
        } catch (AccessDeniedException e) {
            assert e.getMessage().equals("You can only manage your own account or as ADMIN");
        }

        verify(userRepository).existsByIdAndRole(testUserId, UserRole.ADMIN);
    }
}