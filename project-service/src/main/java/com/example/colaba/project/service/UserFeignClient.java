package com.example.colaba.project.service;

import com.example.colaba.user.dto.UserDto;  // ← Импорт DTO из shared (если его нет — создадим ниже)
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", path = "/api/users")  // Имя сервиса из Eureka
public interface UserFeignClient {

    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/exists/{id}")
    boolean existsById(@PathVariable("id") Long id);

    // Добавь, если нужно: @GetMapping("/username/{username}")
    // UserDto getUserByUsername(@PathVariable("username") String username);
}