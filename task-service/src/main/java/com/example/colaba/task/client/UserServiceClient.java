package com.example.colaba.task.client;

import com.example.colaba.shared.webmvc.feign.FeignConfig;
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