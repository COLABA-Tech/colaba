//package com.example.colaba.user.controller;
//
//import com.example.colaba.shared.common.dto.user.UserAuthDto;
//import com.example.colaba.shared.common.dto.user.UserResponse;
//import com.example.colaba.shared.common.entity.UserRole;
//import com.example.colaba.user.dto.user.CreateUserRequest;
//import com.example.colaba.user.entity.User;
//import com.example.colaba.user.repository.UserRepository;
//import com.example.colaba.user.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@ComponentScan(basePackages = {
//        "com.example.colaba.user",
//        "com.example.colaba.shared.common",
//        "com.example.colaba.shared.webflux"
//})
//@TestPropertySource(properties = {
//        "internal.api-key=tvulOBWkyfz+TfDMFKWxiZxsBXy8ODfzqX+4TnNSQD+Z+ihYaNS4n2j+1ios3rRM",
//        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
//        "jwt.expiration=3600000",
//        "jwt.issuer=colaba",
//        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
//        "spring.r2dbc.username=sa",
//        "spring.r2dbc.password=",
//        "spring.r2dbc.driver-class-name=io.r2dbc.h2.H2ConnectionFactory"
//})
//@SpringBootTest
//@AutoConfigureWebTestClient
//@MockitoSettings(strictness = Strictness.LENIENT)
//class UserInternalControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockitoBean
//    private UserService userService;
//
//    @MockitoBean
//    private UserRepository userRepository;
//
//    private WebTestClient internalClient;
//
//    private static final String API_KEY = "tvulOBWkyfz+TfDMFKWxiZxsBXy8ODfzqX+4TnNSQD+Z+ihYaNS4n2j+1ios3rRM";
//
//    private static final Long ADMIN_ID = 1L;
//    private static final Long USER_ID = 2L;
//    private static final Long OTHER_ID = 3L;
//
//    private final User sampleUserEntity = User.builder()
//            .id(USER_ID)
//            .username("testuser")
//            .email("test@example.com")
//            .password("hashedpass")
//            .role(UserRole.USER)
//            .build();
//
//    private final UserAuthDto sampleAuthDto = new UserAuthDto(
//            USER_ID,
//            "testuser",
//            "test@example.com",
//            "hashedpass",
//            "USER"
//    );
//
//    private final UserResponse sampleUserResponse = UserResponse.builder()
//            .id(USER_ID)
//            .username("testuser")
//            .email("test@example.com")
//            .role("USER")
//            .build();
//
//    @BeforeEach
//    void setUp() {
//        internalClient = webTestClient.mutate()
//                .defaultHeader("X-Internal-Api-Key", API_KEY)
//                .build();
//
//        when(userRepository.existsByIdAndRole(ADMIN_ID, UserRole.ADMIN)).thenReturn(Mono.just(true));
//        when(userRepository.existsByIdAndRole(USER_ID, UserRole.ADMIN)).thenReturn(Mono.just(false));
//        when(userRepository.existsByIdAndRole(OTHER_ID, UserRole.ADMIN)).thenReturn(Mono.just(false));
//    }
//
//    // ------------------- Тесты -------------------
//
//    @Test
//    void userExists_success_true() {
//        when(userRepository.existsById(USER_ID)).thenReturn(Mono.just(true));
//
//        internalClient.get()
//                .uri("/api/users/internal/{id}/exists", USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(true);
//    }
//
//    @Test
//    void userExists_success_false() {
//        when(userRepository.existsById(USER_ID)).thenReturn(Mono.just(false));
//
//        internalClient.get()
//                .uri("/api/users/internal/{id}/exists", USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(false);
//    }
//
//    @Test
//    void findForAuthByUsername_success() {
//        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(sampleUserEntity));
//
//        internalClient.get()
//                .uri("/api/users/internal/auth-by-username/{username}", "testuser")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserAuthDto.class).isEqualTo(sampleAuthDto);
//    }
//
//    @Test
//    void findForAuthByUsername_notFound() {
//        when(userRepository.findByUsername("unknown")).thenReturn(Mono.empty());
//
//        internalClient.get()
//                .uri("/api/users/internal/auth-by-username/unknown")
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void findForAuthByEmail_success() {
//        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(sampleUserEntity));
//
//        internalClient.get()
//                .uri("/api/users/internal/auth-by-email/{email}", "test@example.com")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserAuthDto.class).isEqualTo(sampleAuthDto);
//    }
//
//    @Test
//    void findForAuthByEmail_notFound() {
//        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Mono.empty());
//
//        internalClient.get()
//                .uri("/api/users/internal/auth-by-email/unknown@example.com")
//                .exchange()
//                .expectStatus().isNotFound();
//    }
//
//    @Test
//    void createUser_success() {
//        UserAuthDto request = new UserAuthDto(null, "newuser", "new@email.com", "plainpass", "USER");
//        CreateUserRequest serviceRequest = new CreateUserRequest("newuser", "new@email.com", "plainpass", UserRole.USER);
//        UserResponse serviceResponse = UserResponse.builder()
//                .id(4L)
//                .username("newuser")
//                .email("new@email.com")
//                .role("USER")
//                .build();
//        UserAuthDto expected = new UserAuthDto(4L, "newuser", "new@email.com", "plainpass", "USER");
//
//        when(userService.createUser(eq(serviceRequest))).thenReturn(Mono.just(serviceResponse));
//
//        internalClient.post()
//                .uri("/api/users/internal/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserAuthDto.class).isEqualTo(expected);
//    }
//
//    @Test
//    void isAdmin_success_true() {
//        internalClient.get()
//                .uri("/api/users/internal/{id}/is-admin", ADMIN_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(true);
//    }
//
//    @Test
//    void isAdmin_success_false() {
//        internalClient.get()
//                .uri("/api/users/internal/{id}/is-admin", USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(false);
//    }
//
//    @Test
//    void getUserRole_success() {
//        when(userRepository.findRoleById(USER_ID)).thenReturn(Mono.just("USER"));
//
//        internalClient.get()
//                .uri("/api/users/internal/{id}/role", USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(UserRole.class).isEqualTo(UserRole.USER);
//    }
//
//    @Test
//    void getUserRole_notFound() {
//        when(userRepository.findRoleById(999L)).thenReturn(Mono.empty());
//
//        internalClient.get()
//                .uri("/api/users/internal/999/role")
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    @Test
//    void canManageUser_sameUser_success() {
//        internalClient.get()
//                .uri("/api/users/internal/{currentUserId}/can-manage/{targetUserId}", USER_ID, USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(true);
//    }
//
//    @Test
//    void canManageUser_adminCanManageOther_success() {
//        internalClient.get()
//                .uri("/api/users/internal/{currentUserId}/can-manage/{targetUserId}", ADMIN_ID, USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(true);
//    }
//
//    @Test
//    void canManageUser_nonAdminCannotManageOther_success() {
//        internalClient.get()
//                .uri("/api/users/internal/{currentUserId}/can-manage/{targetUserId}", OTHER_ID, USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(false);
//    }
//
//    @Test
//    void canManageUser_nonExistingCurrentUser_false() {
//        when(userRepository.existsByIdAndRole(999L, UserRole.ADMIN)).thenReturn(Mono.just(false));
//
//        internalClient.get()
//                .uri("/api/users/internal/{currentUserId}/can-manage/{targetUserId}", 999L, USER_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(Boolean.class).isEqualTo(false);
//    }
//}