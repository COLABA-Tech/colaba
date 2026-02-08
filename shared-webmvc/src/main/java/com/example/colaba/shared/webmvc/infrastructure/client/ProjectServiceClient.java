package com.example.colaba.shared.webmvc.infrastructure.client;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.infrastructure.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "project-service",
        configuration = FeignConfig.class
)
public interface ProjectServiceClient {
    @GetMapping("/api/projects/internal/{id}/exists")
    boolean projectExists(@PathVariable Long id);

    @GetMapping("/api/tags/internal/{id}")
    TagResponse getTagById(@PathVariable Long id);

    @PostMapping("/api/tags/internal/batch")
    List<TagResponse> getTagsByIds(@RequestBody List<Long> tagIds);

    @GetMapping("/api/projects/internal/{projectId}/user/{userId}/any-role")
    boolean hasAnyRole(@PathVariable("projectId") Long projectId,
                       @PathVariable("userId") Long userId);

    @GetMapping("/api/projects/internal/{projectId}/user/{userId}/at-least-editor")
    boolean isAtLeastEditor(@PathVariable("projectId") Long projectId,
                            @PathVariable("userId") Long userId);

    @GetMapping("/api/projects/internal/{projectId}/user/{userId}/owner")
    boolean isOwner(@PathVariable("projectId") Long projectId,
                    @PathVariable("userId") Long userId);

    @GetMapping("/api/projects/internal/{projectId}/user/{userId}/role")
    String getUserProjectRole(@PathVariable("projectId") Long projectId,
                              @PathVariable("userId") Long userId);
}
