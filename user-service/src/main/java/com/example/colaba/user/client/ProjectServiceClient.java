package com.example.colaba.user.client;

import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.dto.tag.TagResponse;
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
    List<ProjectResponse> findByOwnerId(@PathVariable Long id);

    @DeleteMapping("/api/projects/internal/{id}")
    void deleteProject(@PathVariable Long id);

    @GetMapping("/api/projects/internal/{id}/exists")
    boolean projectExists(@PathVariable Long id);

    @DeleteMapping("/api/projects/internal/user/{userId}/memberships")
    void handleUserDeletion(@PathVariable Long userId);

    @GetMapping("/api/projects/internal/{projectId}/membership/{userId}")
    boolean isMember(@PathVariable Long projectId, @PathVariable Long userId);

    @GetMapping("/api/tags/internal/{id}")
    TagResponse getTagById(@PathVariable Long id);

    @PostMapping("/api/tags/internal/batch")
    List<TagResponse> getTagsByIds(@RequestBody List<Long> tagIds);

    @GetMapping("/api/tags/internal/{id}/exists")
    boolean tagExists(@PathVariable Long id);
}
