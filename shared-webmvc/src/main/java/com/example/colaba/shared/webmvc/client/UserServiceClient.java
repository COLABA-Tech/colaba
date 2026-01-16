package com.example.colaba.shared.webmvc.client;

import com.example.colaba.shared.common.dto.user.UserAuthDto;
import com.example.colaba.shared.webmvc.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        path = "/api/users/internal",
        configuration = FeignConfig.class
)
public interface UserServiceClient {
    @GetMapping("/{id}/exists")
    boolean userExists(@PathVariable Long id);

    @GetMapping("/auth-by-username/{username}")
    UserAuthDto findForAuthByUsername(@PathVariable String username);

    @GetMapping("/auth-by-email/{email}")
    UserAuthDto findForAuthByEmail(@PathVariable String email);

    @PostMapping("/create")
    UserAuthDto createUser(@RequestBody UserAuthDto userAuthDto);
}