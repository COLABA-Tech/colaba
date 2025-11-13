package com.example.colaba.unit.service;

import com.example.colaba.dto.user.CreateUserRequest;
import com.example.colaba.dto.user.UpdateUserRequest;
import com.example.colaba.dto.user.UserResponse;
import com.example.colaba.dto.user.UserScrollResponse;
import com.example.colaba.entity.User;
import com.example.colaba.exception.user.DuplicateUserEntityEmailException;
import com.example.colaba.exception.user.DuplicateUserEntityUsernameException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.mapper.UserMapper;
import com.example.colaba.repository.UserRepository;
import com.example.colaba.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest request;
    private User savedUser;

    private final Long test_id = 1L;
    private final String test_username = "test";
    private final String test_email = "test@colaba.com";

    @BeforeEach
    void setUp() {
        request = new CreateUserRequest(test_username, test_email);
        savedUser = User.builder().id(test_id).username(test_username).email(test_email).build();
    }

    @Test
    void createUser_success() {
        // Given (arrange)
        when(userRepository.existsByUsername(test_username)).thenReturn(false);
        when(userRepository.existsByEmail(test_email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

        // When (act)
        UserResponse result = userService.createUser(request);

        // Then (assert)
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username());
        assertEquals(test_email, result.email());
        verify(userRepository).existsByUsername(test_username);
        verify(userRepository).existsByEmail(test_email);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void createUser_duplicateUsername_throwsException() {
        // Given
        when(userRepository.existsByUsername(test_username)).thenReturn(true);

        // When & Then
        DuplicateUserEntityUsernameException exception = assertThrows(DuplicateUserEntityUsernameException.class,
                () -> userService.createUser(request));
        assertEquals("Duplicate user entity: USERNAME " + test_username, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_duplicateEmail_throwsException() {
        // Given
        when(userRepository.existsByEmail(test_email)).thenReturn(true);

        // When & Then
        DuplicateUserEntityEmailException exception = assertThrows(DuplicateUserEntityEmailException.class,
                () -> userService.createUser(request));
        assertEquals("Duplicate user entity: EMAIL " + test_email, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_success() {
        // Given (arrange)
        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

        // When (act)
        UserResponse result = userService.getUserById(test_id);

        // Then (assert)
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username());
        assertEquals(test_email, result.email());
        verify(userRepository).findById(test_id);
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void getUserById_notFound_throwsException() {
        // Given (arrange)
        when(userRepository.findById(test_id)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(test_id));
        assertEquals("User not found: ID " + test_id, exception.getMessage());
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void getUserEntityById_success() {
        // Given (arrange)
        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));

        // When (act)
        User result = userService.getUserEntityById(test_id);

        // Then (assert)
        assertEquals(test_id, result.getId());
        assertEquals(test_username, result.getUsername());
        assertEquals(test_email, result.getEmail());
        verify(userRepository).findById(test_id);
    }

    @Test
    void getUserEntityById_notFound_throwsException() {
        // Given (arrange)
        when(userRepository.findById(test_id)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserEntityById(test_id));
        assertEquals("User not found: ID " + test_id, exception.getMessage());
    }

    @Test
    void getUserByUsername_success() {
        // Given (arrange)
        when(userRepository.findByUsername(test_username)).thenReturn(Optional.of(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

        // When (act)
        UserResponse result = userService.getUserByUsername(test_username);

        // Then (assert)
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username());
        assertEquals(test_email, result.email());
        verify(userRepository).findByUsername(test_username);
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void getUserByUsername_notFound_throwsException() {
        // Given (arrange)
        when(userRepository.findByUsername(test_username)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByUsername(test_username));
        assertEquals("User not found: USERNAME " + test_username, exception.getMessage());
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void updateUser_success() {
        // Given
        String newUsername = "newUsername";
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(newUsername, newEmail);

        User updatedUser = User.builder().id(test_id).username(newUsername).email(newEmail).build();

        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, newUsername, newEmail));

        // When
        UserResponse result = userService.updateUser(test_id, request);

        // Then
        assertEquals(test_id, result.id());
        assertEquals(newUsername, result.username());
        assertEquals(newEmail, result.email());
        verify(userRepository).findById(test_id);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(updatedUser);
    }

    @Test
    void updateUser_partialUpdate_username_success() {
        // Given
        String newUsername = "newUsername";
        UpdateUserRequest request = new UpdateUserRequest(newUsername, null);

        User updatedUser = User.builder().id(test_id).username(newUsername).email(test_email).build();

        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, newUsername, test_email));

        // When
        UserResponse result = userService.updateUser(test_id, request);

        // Then
        assertEquals(test_id, result.id());
        assertEquals(newUsername, result.username());
        assertEquals(test_email, result.email()); // email unchanged
        verify(userRepository).findById(test_id);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(updatedUser);
    }

    @Test
    void updateUser_partialUpdate_email_success() {
        // Given
        String newEmail = "newemail@colaba.com";
        UpdateUserRequest request = new UpdateUserRequest(null, newEmail);

        User updatedUser = User.builder().id(test_id).username(test_username).email(newEmail).build();

        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toUserResponse(updatedUser)).thenReturn(new UserResponse(test_id, test_username, newEmail));

        // When
        UserResponse result = userService.updateUser(test_id, request);

        // Then
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username()); // username unchanged
        assertEquals(newEmail, result.email());
        verify(userRepository).findById(test_id);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toUserResponse(updatedUser);
    }

    @Test
    void updateUser_blankFields_ignoresBlankValues() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(" ", " ");

        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

        // When
        UserResponse result = userService.updateUser(test_id, request);

        // Then
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username()); // remains unchanged
        assertEquals(test_email, result.email()); // remains unchanged
        verify(userRepository).findById(test_id);
        verify(userRepository, never()).save(any(User.class)); // No save called
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void updateUser_noChanges_returnsUnchangedUser() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest(test_username, test_email);

        when(userRepository.findById(test_id)).thenReturn(Optional.of(savedUser));
        when(userMapper.toUserResponse(savedUser)).thenReturn(new UserResponse(test_id, test_username, test_email));

        // When
        UserResponse result = userService.updateUser(test_id, request);

        // Then
        assertEquals(test_id, result.id());
        assertEquals(test_username, result.username()); // remains unchanged
        assertEquals(test_email, result.email()); // remains unchanged
        verify(userRepository).findById(test_id);
        verify(userRepository, never()).save(any(User.class)); // No save called
        verify(userMapper).toUserResponse(savedUser);
    }

    @Test
    void updateUser_notFound_throwsException() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest("newUsername", "newemail@colaba.com");
        when(userRepository.findById(test_id)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(test_id, request));
        assertEquals("User not found: ID " + test_id, exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void deleteUser_success() {
        // Given
        when(userRepository.existsById(test_id)).thenReturn(true);
        doNothing().when(userRepository).deleteById(test_id);

        // When
        userService.deleteUser(test_id);

        // Then
        verify(userRepository).existsById(test_id);
        verify(userRepository).deleteById(test_id);
    }

    @Test
    void deleteUser_notFound_throwsException() {
        // Given
        when(userRepository.existsById(test_id)).thenReturn(false);

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(test_id));
        assertEquals("User not found: ID " + test_id, exception.getMessage());
        verify(userRepository, never()).deleteById(test_id);
    }

    @Test
    void getAllUsers_pagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(List.of(savedUser));

        when(userRepository.findAll(pageable)).thenReturn(mockPage);
        when(userMapper.toUserResponsePage(mockPage)).thenReturn(new PageImpl<>(List.of(new UserResponse(test_id, test_username, test_email))));

        // When
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(userMapper).toUserResponsePage(mockPage);
    }

    @Test
    void getUsersScroll_withEmptyCursor_returnsFirstPage() {
        // Given
        String cursor = "";
        int limit = 10;
        long expectedOffset = 0L;
        String expectedNextCursor = "1";

        List<User> users = List.of(savedUser);
        Slice<User> mockSlice = new SliceImpl<>(users, PageRequest.of(0, limit), true);

        when(userRepository.findAllByOffset(expectedOffset, limit)).thenReturn(mockSlice);
        when(userMapper.toUserResponseList(users)).thenReturn(List.of(new UserResponse(test_id, test_username, test_email)));

        // When
        UserScrollResponse result = userService.getUsersScroll(cursor, limit);

        // Then
        assertEquals(1, result.users().size());
        assertEquals(expectedNextCursor, result.nextCursor());
        assertTrue(result.hasMore());
        verify(userRepository).findAllByOffset(expectedOffset, limit);
        verify(userMapper).toUserResponseList(users);
    }

    @Test
    void getUsersScroll_withCursor_returnsNextPage() {
        // Given
        String cursor = "5";
        int limit = 10;
        long expectedOffset = 5L;
        String expectedNextCursor = "6";

        List<User> users = List.of(savedUser);
        Slice<User> mockSlice = new SliceImpl<>(users, PageRequest.of(0, limit), false);

        when(userRepository.findAllByOffset(expectedOffset, limit)).thenReturn(mockSlice);
        when(userMapper.toUserResponseList(users)).thenReturn(List.of(new UserResponse(test_id, test_username, test_email)));

        // When
        UserScrollResponse result = userService.getUsersScroll(cursor, limit);

        // Then
        assertEquals(1, result.users().size());
        assertEquals(expectedNextCursor, result.nextCursor());
        assertFalse(result.hasMore());
        verify(userRepository).findAllByOffset(expectedOffset, limit);
        verify(userMapper).toUserResponseList(users);
    }
}
