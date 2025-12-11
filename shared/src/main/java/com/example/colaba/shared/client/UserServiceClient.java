package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        path = "/api/users/internal",
        configuration = FeignConfig.class
)
public interface UserServiceClient {
    @GetMapping("/entity/{id}")
    User getUserEntityById(@PathVariable Long id);

    @GetMapping("/api/users/{id}/exists")
    boolean userExists(@PathVariable Long id);
}