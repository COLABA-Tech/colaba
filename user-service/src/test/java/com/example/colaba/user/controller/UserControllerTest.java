package com.example.colaba.user.controller;

import com.example.colaba.shared.common.dto.user.UserResponse;
import com.example.colaba.shared.common.entity.UserRole;
import com.example.colaba.user.dto.user.UpdateUserRequest;
import com.example.colaba.user.dto.user.UserScrollResponse;
import com.example.colaba.user.repository.UserRepository;
import com.example.colaba.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ComponentScan(basePackages = {
        "com.example.colaba.user",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webflux"
})
@TestPropertySource(properties = {
        "internal.api-key'=supersecretbase64valuehereatleast32byteslong==",
        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
        "jwt.expiration=3600000",
        "jwt.issuer=colaba",
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.r2dbc.driver-class-name=io.r2dbc.h2.H2ConnectionFactory"
})
@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    private static final Long ADMIN_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long OTHER_ID = 3L;

    private final UserResponse sampleUser = UserResponse.builder()
            .id(USER_ID)
            .username("testuser")
            .email("test@example.com")
            .role("USER")
            .build();

    @BeforeEach
    void setUp() {
        // Настраиваем поведение access checker (для всех тестов)
        when(userRepository.existsByIdAndRole(ADMIN_ID, UserRole.ADMIN)).thenReturn(Mono.just(true));
        when(userRepository.existsByIdAndRole(USER_ID, UserRole.ADMIN)).thenReturn(Mono.just(false));
        when(userRepository.existsByIdAndRole(OTHER_ID, UserRole.ADMIN)).thenReturn(Mono.just(false));
    }

    private WebTestClient authenticatedClient(Long userId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        return webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth));
    }

    // ------------------- Тесты -------------------

    @Test
    void getUserByUsername_success() {
        when(userService.getUserByUsername("testuser")).thenReturn(Mono.just(sampleUser));

        authenticatedClient(USER_ID)
                .get()
                .uri("/api/users/testuser")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class).isEqualTo(sampleUser);
    }

    @Test
    void getAllUsers_asAdmin_success() {
        Page<UserResponse> page = new PageImpl<>(List.of(sampleUser), PageRequest.of(0, 20), 1);
        when(userService.getAllUsers(any())).thenReturn(Mono.just(page));

        authenticatedClient(ADMIN_ID)
                .get()
                .uri("/api/users?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.content[0].username").isEqualTo("testuser");
    }

    @Test
    void getAllUsers_asNonAdmin_forbidden() {
        authenticatedClient(USER_ID)
                .get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getUsersScroll_asAdmin_success() {
        UserScrollResponse response = new UserScrollResponse(List.of(sampleUser), "20", true);
        when(userService.getUsersScroll("0", 20)).thenReturn(Mono.just(response));

        authenticatedClient(ADMIN_ID)
                .get()
                .uri("/api/users/scroll?cursor=0&limit=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserScrollResponse.class).isEqualTo(response);
    }

    @Test
    void getUsersScroll_limitCapped() {
        UserScrollResponse response = new UserScrollResponse(List.of(), "0", false);
        when(userService.getUsersScroll(anyString(), eq(50))).thenReturn(Mono.just(response));

        authenticatedClient(ADMIN_ID)
                .get()
                .uri("/api/users/scroll?limit=100")  // должно cap на 50
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateUser_asSelf_success() {
        UpdateUserRequest req = new UpdateUserRequest("newuser", "new@email.com", null);
        when(userService.updateUser(eq(USER_ID), any(UpdateUserRequest.class))).thenReturn(Mono.just(sampleUser));

        authenticatedClient(USER_ID)
                .put()
                .uri("/api/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateUser_asOtherUser_forbidden() {
        authenticatedClient(OTHER_ID)
                .put()
                .uri("/api/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateUserRequest("new", "new@email.com", null))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateUser_asAdmin_success() {
        when(userService.updateUser(eq(USER_ID), any())).thenReturn(Mono.just(sampleUser));

        authenticatedClient(ADMIN_ID)
                .put()
                .uri("/api/users/" + USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new UpdateUserRequest("new", "new@email.com", null))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteUser_asAdmin_success() {
        when(userService.deleteUser(anyLong())).thenReturn(Mono.empty());

        authenticatedClient(ADMIN_ID)
                .delete()
                .uri("/api/users/" + USER_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteUser_asNonAdmin_forbidden() {
        authenticatedClient(USER_ID)
                .delete()
                .uri("/api/users/" + OTHER_ID)
                .exchange()
                .expectStatus().isForbidden();
    }
}