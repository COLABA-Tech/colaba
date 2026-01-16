package com.example.colaba.task.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.task.service.TaskServicePublic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-tags")
@RequiredArgsConstructor
@Tag(name = "Task Assignment Public", description = "API for managing task-tag associations")
public class TagAssignmentController extends BaseController {
    private final TaskServicePublic taskService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get tags for a task", description = "Returns all tags assigned to the specified task. Requires any role in the project.")
    public ResponseEntity<List<TagResponse>> getTagsByTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal Long currentUserId) {

        List<TagResponse> tags = taskService.getTagsByTask(taskId, currentUserId);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Assign tag to task", description = "Assigns an existing tag to a task. Requires at least EDITOR role in the project.")
    public ResponseEntity<Void> assignTagToTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId,
            @AuthenticationPrincipal Long currentUserId) {

        taskService.assignTagToTask(taskId, tagId, currentUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Remove tag from task", description = "Removes a tag from a task. Requires at least EDITOR role in the project.")
    public ResponseEntity<Void> removeTagFromTask(
            @PathVariable Long taskId,
            @PathVariable Long tagId,
            @AuthenticationPrincipal Long currentUserId) {

        taskService.removeTagFromTask(taskId, tagId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
