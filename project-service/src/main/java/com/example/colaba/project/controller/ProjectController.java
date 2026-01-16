package com.example.colaba.project.controller;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.service.ProjectServicePublic;
import com.example.colaba.project.service.TagServicePublic;
import com.example.colaba.shared.common.controller.BaseController;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects Public", description = "API for managing projects")
public class ProjectController extends BaseController {

    private final ProjectServicePublic projectService;
    private final TagServicePublic tagService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the provided name, description, and owner ID. Validates for unique name and existing owner.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate project name"),
            @ApiResponse(responseCode = "404", description = "Owner user not found")
    })
    public Mono<ResponseEntity<ProjectResponse>> create(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return projectService.createProject(request, currentUserId)
                .map(projectResponse -> ResponseEntity
                        .created(URI.create("/api/projects/" + projectResponse.id()))
                        .body(projectResponse));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Partially updates a project by ID (name or description). Validates for duplicate names and applies changes only if different.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate project name"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEntity<ProjectResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal Long currentUserId) {
        return projectService.updateProject(id, request, currentUserId)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/owner")
    @Operation(summary = "Change project owner", description = "Changes the owner of a project to a new user by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project owner changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing ownerId)"),
            @ApiResponse(responseCode = "404", description = "Project or new owner not found")
    })
    public Mono<ResponseEntity<ProjectResponse>> changeOwner(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request,
            @AuthenticationPrincipal Long currentUserId) {
        Long newOwnerId = request.get("ownerId");
        if (newOwnerId == null) {
            return Mono.error(new IllegalArgumentException("ownerId is required"));
        }
        return projectService.changeProjectOwner(id, newOwnerId, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEntity<ProjectResponse>> getById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long currentUserId) {
        return projectService.getProjectById(id, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    @Operation(summary = "Get all projects (non-paginated)", description = "Retrieves all projects without pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all projects")
    })
    public Mono<ResponseEntity<Page<ProjectResponse>>> getAll(
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        return projectService.getAllProjects(pageable, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get projects by owner ID", description = "Retrieves a list of projects owned by a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of projects for the owner"),
            @ApiResponse(responseCode = "404", description = "Owner user not found")
    })
    public Mono<ResponseEntity<List<ProjectResponse>>> getByOwner(
            @PathVariable("ownerId") Long ownerId,
            @AuthenticationPrincipal Long currentUserId) {
        return projectService.getProjectByOwnerId(ownerId, currentUserId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/scroll")
    @Operation(summary = "Get projects with scrolling pagination", description = "Retrieves projects for scrolling pagination (page and size). Parameters page/size are optional with defaults. Size capped at 50.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scroll response with projects, hasNext, and total"),
            @ApiResponse(responseCode = "400", description = "Invalid page size > 50")
    })
    public Mono<ResponseEntity<ProjectScrollResponse>> scroll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size,
            @AuthenticationPrincipal Long currentUserId) {
        if (size > 50) size = 50;
        return projectService.scroll(page, size, currentUserId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Deletes a project by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long currentUserId) {
        projectService.deleteProject(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "Get tags by project ID with pagination", description = "Retrieves a paginated list of tags for a specific project. Supports standard Spring Pageable parameters. TODO: Move to project controller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tags for the project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEntity<Page<TagResponse>>> getTagsByProject(
            @PathVariable Long id,
            Pageable pageable,
            @AuthenticationPrincipal Long currentUserId) {
        pageable = validatePageable(pageable);
        return tagService.getTagsByProject(id, pageable, currentUserId)
                .map(ResponseEntity::ok);
    }
}