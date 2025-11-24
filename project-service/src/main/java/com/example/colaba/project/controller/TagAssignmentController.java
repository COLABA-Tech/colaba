package com.example.colaba.project.controller;

import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.project.service.TagService;
import com.example.colaba.shared.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/task-tags")
@RequiredArgsConstructor
@Tag(name = "Task Tags", description = "API for managing task-tag associations")
public class TagAssignmentController extends BaseController {
    private final TagService tagService;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get tags for a task")
    public ResponseEntity<Iterable<TagResponse>> getTagsByTask(@PathVariable Long taskId) {
        Iterable<TagResponse> tags = tagService.getTagsByTask(taskId);
        return ResponseEntity.ok(tags);
    }

    @PostMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Assign tag to task")
    public ResponseEntity<Void> assignTagToTask(
            @PathVariable Long taskId, @PathVariable Long tagId) {
        tagService.assignTagToTask(taskId, tagId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/tag/{tagId}")
    @Operation(summary = "Remove tag from task")
    public ResponseEntity<Void> removeTagFromTask(
            @PathVariable Long taskId, @PathVariable Long tagId) {
        tagService.removeTagFromTask(taskId, tagId);
        return ResponseEntity.noContent().build();
    }
}
