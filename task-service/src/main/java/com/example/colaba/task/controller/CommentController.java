package com.example.colaba.task.controller;

import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.service.CommentServicePublic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments Public", description = "API for managing comments")
public class CommentController extends BaseController {

    private final CommentServicePublic commentService;

    @PostMapping
    @Operation(summary = "Create a new comment for a task")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "User doesn't have access to the task")
    public ResponseEntity<CommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        CommentResponse commentResponse = commentService.createComment(request, currentUserId);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequestUri()
                        .path("/{id}")
                        .buildAndExpand(commentResponse.id())
                        .toUri()
        ).body(commentResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID")
    @ApiResponse(responseCode = "200", description = "Comment found")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "403", description = "User doesn't have access to the task")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal Long currentUserId) {

        CommentResponse comment = commentService.getCommentById(id, currentUserId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get paginated comments by task ID")
    @ApiResponse(responseCode = "200", description = "Paginated comments")
    @ApiResponse(responseCode = "403", description = "User doesn't have access to the task")
    public ResponseEntity<Page<CommentResponse>> getCommentsByTask(
            @PathVariable @Positive Long taskId,
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {

        pageable = validatePageable(pageable);
        Page<CommentResponse> comments = commentService.getCommentsByTask(taskId, pageable, currentUserId);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(comments.getTotalElements()))
                .body(comments);
    }

    @GetMapping("/task/{taskId}/scroll")
    @Operation(summary = "Get infinite scroll comments by task ID")
    @ApiResponse(responseCode = "200", description = "Scroll response with hasMore")
    @ApiResponse(responseCode = "403", description = "User doesn't have access to the task")
    public ResponseEntity<CommentScrollResponse> getCommentsScrollByTask(
            @PathVariable @Positive Long taskId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal Long currentUserId) {

        if (limit > 50) limit = 50;
        CommentScrollResponse response = commentService.getCommentsByTaskScroll(taskId, cursor, limit, currentUserId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comment content")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "403", description = "User is not the author of the comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal Long currentUserId) {

        CommentResponse updated = commentService.updateComment(id, request, currentUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment by ID")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    @ApiResponse(responseCode = "403", description = "User doesn't have permission to delete the comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable @Positive Long id,
            @AuthenticationPrincipal Long currentUserId) {

        commentService.deleteComment(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}