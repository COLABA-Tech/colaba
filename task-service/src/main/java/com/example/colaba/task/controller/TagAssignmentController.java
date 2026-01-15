package com.example.colaba.task.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-tags")
@RequiredArgsConstructor
@Tag(name = "Task Assignment Public", description = "API for managing task-tag associations")
public class TagAssignmentController extends BaseController {
    private final TaskService taskService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get tags for a task")
    public ResponseEntity<List<TagResponse>> getTagsByTask(@PathVariable Long taskId) {
        List<TagResponse> tags = taskService.getTagsByTask(taskId);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Assign tag to task")
    public ResponseEntity<Void> assignTagToTask(
            @PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.assignTagToTask(taskId, tagId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Remove tag from task")
    public ResponseEntity<Void> removeTagFromTask(
            @PathVariable Long taskId, @PathVariable Long tagId) {
        taskService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }
}
