package com.example.colaba.user.controller;

import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/internal")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserRepository userRepository;

    @GetMapping("/{id}/exists")
    public boolean projectExists(@PathVariable Long id) {
        return Boolean.TRUE.equals(userRepository.existsById(id).block());
    }
}