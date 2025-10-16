package com.example.colaba.controller;

import com.example.colaba.dto.CreateUserRequest;
import com.example.colaba.dto.UpdateUserRequest;
import com.example.colaba.dto.UserResponse;
import com.example.colaba.dto.UserScrollResponse;
import com.example.colaba.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse userResponse = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    // Paginated findAll
    // Auto-handles ?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    // Infinite scroll endpoint
    @GetMapping("/scroll")
    public ResponseEntity<UserScrollResponse> getUsersScroll(@RequestParam(defaultValue = "0") String cursor,
                                                             @RequestParam(defaultValue = "20") int limit) {
        if (limit > 50) limit = 50;  // Enforce max
        UserScrollResponse response = userService.getUsersScroll(cursor, limit);
        return ResponseEntity.ok(response);
    }

    // New: Paginated with total header
    @GetMapping("/paginated")
    public ResponseEntity<Page<UserResponse>> getUsersPaginated(Pageable pageable) {
        if (pageable.getPageSize() > 50) {
            pageable = PageRequest.of(pageable.getPageNumber(), 50, pageable.getSort());
        }
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(users.getTotalElements()))
                .body(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
