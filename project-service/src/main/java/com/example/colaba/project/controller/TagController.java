package com.example.colaba.project.controller;

import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.service.TagServicePublic;
import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags Public", description = "API for managing tags")
public class TagController extends BaseController {
    private final TagServicePublic tagService;

    @GetMapping
    @Operation(summary = "Get all tags with pagination", description = "Retrieves a paginated list of all tags from projects where user is a member. Supports standard Spring Pageable parameters (page, size, sort).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tags")
    })
    public Mono<ResponseEntity<Page<TagResponse>>> getAllTags(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        return tagService.getAllTags(pageable, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Retrieves a specific tag by its ID. User must be a member of the tag's project.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    public Mono<ResponseEntity<TagResponse>> getTagById(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return tagService.getTagById(id, currentUserId)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Create a new tag", description = "Creates a new tag with the provided details. User must be at least EDITOR in the project.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to create tags in this project")
    })
    public Mono<ResponseEntity<TagResponse>> createTag(
            @Valid @RequestBody CreateTagRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return tagService.createTag(request, currentUserId)
                .map(tag -> ResponseEntity.status(HttpStatus.CREATED).body(tag));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tag", description = "Partially updates a tag by ID. User must be at least EDITOR in the tag's project.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to update this tag")
    })
    public Mono<ResponseEntity<TagResponse>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return tagService.updateTag(id, request, currentUserId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag", description = "Deletes a tag by ID. User must be at least EDITOR in the tag's project.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "403", description = "User doesn't have permission to delete this tag")
    })
    public Mono<ResponseEntity<Void>> deleteTag(
            @PathVariable Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return tagService.deleteTag(id, currentUserId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}