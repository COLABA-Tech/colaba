package com.example.colaba.shared.client;

import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "project-service",
        configuration = FeignConfig.class
)
public interface ProjectServiceClient {
    @GetMapping("/api/projects/internal/owner/{id}")
    List<Project> findByOwnerId(@PathVariable Long id);

    @DeleteMapping("/api/projects/internal/{id}")
    void deleteProject(@PathVariable Long id);

    @GetMapping("/api/projects/internal/{id}/exists")
    boolean projectExists(@PathVariable Long id);

    @GetMapping("/api/tags/internal/{id}")
    TagResponse getTagById(@PathVariable Long id);

    @PostMapping("/api/tags/internal/batch")
    List<TagResponse> getTagsByIds(@RequestBody List<Long> tagIds);

    @GetMapping("/api/tags/internal/{id}/exists")
    boolean tagExists(@PathVariable Long id);
}
