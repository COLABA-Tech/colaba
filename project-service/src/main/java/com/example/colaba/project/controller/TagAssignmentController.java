package com.example.colaba.project.controller;

import com.example.colaba.project.service.TagService;
import com.example.colaba.shared.controller.BaseController;
import com.example.colaba.shared.dto.tag.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/task-tags")
@RequiredArgsConstructor
@Tag(name = "Task Tags", description = "API for managing task-tag associations")
public class TagAssignmentController extends BaseController {
    private final TagService tagService;

    // TODO move to task
//    @GetMapping("/task/{taskId}")
//    @Operation(summary = "Get tags for a task")
//    public Mono<ResponseEntity<List<TagResponse>>> getTagsByTask(@PathVariable Long taskId) {
//        return tagService.getTagsByTask(taskId)
//                .map(ResponseEntity::ok);
//    }

//    @PostMapping("/task/{taskId}/tag/{tagId}")
//    @Operation(summary = "Assign tag to task")
//    public Mono<ResponseEntity<Void>> assignTagToTask(
//            @PathVariable Long taskId, @PathVariable Long tagId) {
//        return tagService.assignTagToTask(taskId, tagId)
//                .then(Mono.just(ResponseEntity.ok().build()));
//    }

//    @DeleteMapping("/task/{taskId}/tag/{tagId}")
//    @Operation(summary = "Remove tag from task")
//    public Mono<ResponseEntity<Void>> removeTagFromTask(
//            @PathVariable Long taskId, @PathVariable Long tagId) {
//        return tagService.removeTagFromTask(taskId, tagId)
//                .then(Mono.just(ResponseEntity.noContent().build()));
//    }
}
