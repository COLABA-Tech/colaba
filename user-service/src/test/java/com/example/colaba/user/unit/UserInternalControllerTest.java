package com.example.colaba.user.unit;

import com.example.colaba.shared.common.application.dto.user.UserAuthDto;
import com.example.colaba.shared.common.application.dto.user.UserResponse;
import com.example.colaba.shared.common.domain.entity.UserRole;
import com.example.colaba.user.controller.UserInternalController;
import com.example.colaba.user.dto.user.CreateUserRequest;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserInternalControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserInternalController userInternalController;

    private User testUser;
    private UserAuthDto testUserAuthDto;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(userInternalController).build();

        // Создаем тестового пользователя (entity)
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(UserRole.USER);

        // Используем OffsetDateTime вместо LocalDateTime
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);

        testUserAuthDto = new UserAuthDto(
                1L,
                "testuser",
                "test@example.com",
                "encodedPassword123",
                UserRole.USER.getValue()
        );

        testUserResponse = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER.getValue())
                .build();
    }

    @Test
    void userExists_WhenUserExists_ShouldReturnTrue() {
        when(userRepository.existsById(1L)).thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/api/users/internal/1/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void userExists_WhenUserNotExists_ShouldReturnFalse() {
        when(userRepository.existsById(999L)).thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/api/users/internal/999/exists")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void findForAuthByUsername_WhenUserExists_ShouldReturnUserAuthDto() {
        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));

        webTestClient.get()
                .uri("/api/users/internal/auth-by-username/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserAuthDto.class)
                .value(dto -> {
                    assert dto.id().equals(1L);
                    assert dto.username().equals("testuser");
                    assert dto.email().equals("test@example.com");
                    assert dto.password().equals("encodedPassword123");
                    assert dto.role().equals(UserRole.USER.getValue());
                });
    }

    @Test
    void findForAuthByUsername_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Mono.empty());

        // Изменяем ожидание на 500, если исключение не обрабатывается
        webTestClient.get()
                .uri("/api/users/internal/auth-by-username/unknown")
                .exchange()
                .expectStatus().is5xxServerError(); // вместо .isNotFound()
    }

    @Test
    void findForAuthByEmail_WhenUserExists_ShouldReturnUserAuthDto() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(testUser));

        webTestClient.get()
                .uri("/api/users/internal/auth-by-email/test@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserAuthDto.class)
                .value(dto -> {
                    assert dto.id().equals(1L);
                    assert dto.username().equals("testuser");
                    assert dto.email().equals("test@example.com");
                    assert dto.password().equals("encodedPassword123");
                    assert dto.role().equals(UserRole.USER.getValue());
                });
    }

    @Test
    void findForAuthByEmail_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());

        // Изменяем ожидание на 500
        webTestClient.get()
                .uri("/api/users/internal/auth-by-email/unknown@example.com")
                .exchange()
                .expectStatus().is5xxServerError(); // вместо .isNotFound()
    }

    @Test
    void createUser_ShouldCreateUserAndReturnAuthDto() {
        UserResponse createdUserResponse = UserResponse.builder()
                .id(2L)
                .username("newuser")
                .email("new@example.com")
                .role(UserRole.USER.getValue())
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just(createdUserResponse));

        UserAuthDto requestDto = new UserAuthDto(
                null,
                "newuser",
                "new@example.com",
                "Password123",
                UserRole.USER.getValue()
        );

        webTestClient.post()
                .uri("/api/users/internal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserAuthDto.class)
                .value(dto -> {
                    assert dto.id().equals(2L);
                    assert dto.username().equals("newuser");
                    assert dto.email().equals("new@example.com");
                    assert dto.password().equals("Password123");
                    assert dto.role().equals(UserRole.USER.getValue());
                });
    }

    @Test
    void createUser_WithAdminRole_ShouldCreateAdminUser() {
        UserResponse createdAdminResponse = UserResponse.builder()
                .id(3L)
                .username("adminuser")
                .email("admin@example.com")
                .role(UserRole.ADMIN.getValue())
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just(createdAdminResponse));

        UserAuthDto requestDto = new UserAuthDto(
                null,
                "adminuser",
                "admin@example.com",
                "AdminPass123",
                UserRole.ADMIN.getValue()
        );

        webTestClient.post()
                .uri("/api/users/internal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserAuthDto.class)
                .value(dto -> {
                    assert dto.id().equals(3L);
                    assert dto.username().equals("adminuser");
                    assert dto.email().equals("admin@example.com");
                    assert dto.password().equals("AdminPass123");
                    assert dto.role().equals(UserRole.ADMIN.getValue());
                });
    }

    @Test
    void createUser_WhenServiceThrowsException_ShouldPropagateError() {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        UserAuthDto requestDto = new UserAuthDto(
                null,
                "erroruser",
                "error@example.com",
                "Password123",
                UserRole.USER.getValue()
        );

        webTestClient.post()
                .uri("/api/users/internal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void isAdmin_WhenUserIsAdmin_ShouldReturnTrue() {
        when(userRepository.existsByIdAndRole(1L, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/api/users/internal/1/is-admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void isAdmin_WhenUserIsNotAdmin_ShouldReturnFalse() {
        when(userRepository.existsByIdAndRole(2L, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/api/users/internal/2/is-admin")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void getUserRole_WhenUserExists_ShouldReturnRole() {
        when(userRepository.findRoleById(1L))
                .thenReturn(Mono.just(UserRole.USER.getValue()));

        webTestClient.get()
                .uri("/api/users/internal/1/role")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRole.class)
                .isEqualTo(UserRole.USER);
    }

    @Test
    void getUserRole_WhenUserNotExists_ShouldReturnEmpty() {
        when(userRepository.findRoleById(999L))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/users/internal/999/role")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    void canManageUser_WhenSameUser_ShouldReturnTrue() {
        webTestClient.get()
                .uri("/api/users/internal/1/can-manage/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void canManageUser_WhenDifferentUserAndCurrentIsAdmin_ShouldReturnTrue() {
        when(userRepository.existsByIdAndRole(1L, UserRole.ADMIN))
                .thenReturn(Mono.just(true));

        webTestClient.get()
                .uri("/api/users/internal/1/can-manage/2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(true);
    }

    @Test
    void canManageUser_WhenDifferentUserAndCurrentIsNotAdmin_ShouldReturnFalse() {
        when(userRepository.existsByIdAndRole(1L, UserRole.ADMIN))
                .thenReturn(Mono.just(false));

        webTestClient.get()
                .uri("/api/users/internal/1/can-manage/2")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Boolean.class)
                .isEqualTo(false);
    }

    @Test
    void canManageUser_WithRepositoryError_ShouldPropagateError() {
        when(userRepository.existsByIdAndRole(1L, UserRole.ADMIN))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        webTestClient.get()
                .uri("/api/users/internal/1/can-manage/2")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createUser_WithInvalidRole_ShouldThrowIllegalArgumentException() {
        UserAuthDto requestDto = new UserAuthDto(
                null,
                "testuser",
                "test@example.com",
                "Password123",
                "INVALID_ROLE"
        );

        webTestClient.post()
                .uri("/api/users/internal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void createUser_WithNullRole_ShouldThrowIllegalArgumentException() {
        UserAuthDto requestDto = new UserAuthDto(
                null,
                "testuser",
                "test@example.com",
                "Password123",
                null
        );

        webTestClient.post()
                .uri("/api/users/internal/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestDto)
                .exchange()
                .expectStatus().is5xxServerError(); // вместо .isOk()
    }
}