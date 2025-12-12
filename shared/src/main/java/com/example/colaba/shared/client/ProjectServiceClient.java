package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.feign.FeignConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "project-service",
        path = "/api/projects/internal",
        configuration = FeignConfig.class
)
@CircuitBreaker(name = "project-service-cb")
public interface ProjectServiceClient {

    @PostMapping("/owner")
    List<Project> findByOwner(@RequestBody UserJpa owner);

    @DeleteMapping("/all")
    void deleteAll();

    @DeleteMapping("/{id}")
    void deleteProject(@PathVariable Long id);

    @GetMapping("/entity/{id}")
    Project getProjectEntityById(@PathVariable Long id);
}
