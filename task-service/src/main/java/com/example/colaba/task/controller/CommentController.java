package com.example.colaba.task.controller;

import com.example.colaba.shared.controller.BaseController;
import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.dto.comment.CommentScrollResponse;
import com.example.colaba.task.dto.comment.CreateCommentRequest;
import com.example.colaba.task.dto.comment.UpdateCommentRequest;
import com.example.colaba.task.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "API for managing comments")
public class CommentController extends BaseController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Create a new comment for a task")
    @ApiResponse(responseCode = "201", description = "Comment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        CommentResponse commentResponse = commentService.createComment(request);
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
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable @Positive Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }


    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get paginated comments by task ID")
    @ApiResponse(responseCode = "200", description = "Paginated comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsByTask(
            @PathVariable @Positive Long taskId,
            Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<CommentResponse> comments = commentService.getCommentsByTask(taskId, pageable);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(comments.getTotalElements()))
                .body(comments);
    }

    @GetMapping("/task/{taskId}/scroll")
    @Operation(summary = "Get infinite scroll comments by task ID")
    @ApiResponse(responseCode = "200", description = "Scroll response with hasMore")
    public ResponseEntity<CommentScrollResponse> getCommentsScrollByTask(
            @PathVariable @Positive Long taskId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit > 50) limit = 50;
        CommentScrollResponse response = commentService.getCommentsByTaskScroll(taskId, cursor, limit);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comment content")
    @ApiResponse(responseCode = "200", description = "Comment updated")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentResponse updated = commentService.updateComment(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment by ID")
    @ApiResponse(responseCode = "204", description = "Comment deleted")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<Void> deleteComment(@PathVariable @Positive Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}