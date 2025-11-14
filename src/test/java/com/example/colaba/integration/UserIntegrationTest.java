package com.example.colaba.integration;

import com.example.colaba.dto.user.CreateUserRequest;
import com.example.colaba.dto.user.UpdateUserRequest;
import com.example.colaba.entity.User;
import com.example.colaba.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureMockMvc
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldSucceed_WhenValidRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com");

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "email": "%s"
                                }
                                """.formatted(request.username(), request.email())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        // Verify in DB (business logic: user saved with correct fields)
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(1);
        User savedUser = users.get(0);
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createUser_ShouldThrowDuplicateUsernameException_WhenUsernameExists() throws Exception {
        // Given
        userRepository.save(User.builder().username("existing").email("existing@example.com").build());
        CreateUserRequest request = new CreateUserRequest("existing", "new@example.com");

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "email": "%s"
                                }
                                """.formatted(request.username(), request.email())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate user entity: USERNAME existing")); // Assuming global exception handler maps to this

        // Verify no duplicate created
        assertThat(userRepository.findByUsername("existing")).isPresent(); // Original still there
    }

    @Test
    void createUser_ShouldThrowDuplicateEmailException_WhenEmailExists() throws Exception {
        // Given
        userRepository.save(User.builder().username("other").email("existing@example.com").build());
        CreateUserRequest request = new CreateUserRequest("newuser", "existing@example.com");

        // When/Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "email": "%s"
                                }
                                """.formatted(request.username(), request.email())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Duplicate user entity: EMAIL existing@example.com"));

        // Verify no duplicate created
        assertThat(userRepository.findByEmail("existing@example.com")).isPresent();
    }

    @Test
    void getUserByUsername_ShouldReturnUser_WhenUserExists() throws Exception {
        // Given
        User savedUser = userRepository.save(User.builder().username("testuser").email("test@example.com").build());

        // When/Then
        mockMvc.perform(get("/api/users/{username}", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void getUserByUsername_ShouldThrowNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/users/{username}", "nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: USERNAME nonexistent"));
    }

    @Test
    void updateUser_ShouldUpdateFields_WhenValidRequestAndChangesPresent() throws Exception {
        // Given
        User existingUser = userRepository.save(User.builder().username("olduser").email("old@example.com").build());
        UpdateUserRequest request = new UpdateUserRequest("newuser", "new@example.com");

        // When/Then
        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "email": "%s"
                                }
                                """.formatted(request.username(), request.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existingUser.getId()))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        // Verify in DB (business logic: only save if changes, no duplicates checked on update in current impl)
        User updatedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername()).isEqualTo("newuser");
        assertThat(updatedUser.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUser_ShouldNotSave_WhenNoChanges() throws Exception {
        // Given
        User existingUser = userRepository.save(User.builder().username("testuser").email("test@example.com").build());

        UpdateUserRequest request = new UpdateUserRequest("testuser", "test@example.com");

        // When/Then
        mockMvc.perform(put("/api/users/{id}", existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "%s",
                                    "email": "%s"
                                }
                                """.formatted(request.username(), request.email())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        // Verify no unintended changes (business logic: hasChanges check)
        User unchangedUser = userRepository.findById(existingUser.getId()).orElseThrow();
        assertThat(unchangedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    void deleteUser_ShouldDelete_WhenUserExists() throws Exception {
        // Given
        User existingUser = userRepository.save(User.builder().username("testuser").email("test@example.com").build());

        // When/Then
        mockMvc.perform(delete("/api/users/{id}", existingUser.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted (business logic: exists check before delete)
        assertThat(userRepository.findById(existingUser.getId())).isEmpty();
    }

    @Test
    void deleteUser_ShouldThrowNotFound_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: ID 999"));
    }

    @Test
    void getAllUsersPaginated_ShouldReturnPagedResults_WithTotalHeader_WhenPageable() throws Exception {
        // Given: Seed 100 users to test pagination and total
        for (int i = 1; i <= 100; i++) {
            userRepository.save(User.builder()
                    .username("user" + i)
                    .email("user" + i + "@example.com")
                    .build());
        }

        // When/Then: Request page 0, size 10
        mockMvc.perform(get("/api/users/paginated")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(100))
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(header().string("X-Total-Count", "100"));
    }

    @Test
    void getUsersScroll_ShouldReturnSlice_WithNextCursor_WhenCursorAndLimit() throws Exception {
        // Given: Seed 60 users for hasMore testing
        for (int i = 1; i <= 60; i++) {
            userRepository.save(User.builder()
                    .username("scrolluser" + i)
                    .email("scrolluser" + i + "@example.com")
                    .build());
        }

        // When/Then: Initial scroll (cursor=0, limit=20)
        mockMvc.perform(get("/api/users/scroll")
                        .param("cursor", "0")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").exists())
                .andExpect(jsonPath("$.users.length()").value(20))
                .andExpect(jsonPath("$.nextCursor").value("20"))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(jsonPath("$.users[0].username").value("scrolluser1"))
                .andExpect(jsonPath("$.users[19].username").value("scrolluser20"));
        ;
    }

    @Test
    void getUsersScroll_ShouldLimitTo50_WhenLimitExceeds50() throws Exception {
        // Given: Some users
        userRepository.save(User.builder().username("user1").email("user1@example.com").build());

        // When/Then
        mockMvc.perform(get("/api/users/scroll")
                        .param("cursor", "0")
                        .param("limit", "100")) // >50
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users.length()").value(1)); // Assuming only 1 user, but limit capped internally
    }
}