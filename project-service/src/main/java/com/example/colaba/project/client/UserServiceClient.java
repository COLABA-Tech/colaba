package com.example.colaba.project.client;

import com.example.colaba.shared.common.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        path = "/api/users/internal",
        configuration = FeignConfig.class
)
public interface UserServiceClient {
    @GetMapping("/{id}/exists")
    boolean userExists(@PathVariable Long id);
}