package com.example.colaba.shared.client;

import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "project-service", path = "/internal/projects")
public interface ProjectServiceClient {

    @GetMapping("/owner")
    List<Project> findByOwner(@RequestBody UserJpa owner);

    @DeleteMapping("/all")
    void deleteAll();
}
