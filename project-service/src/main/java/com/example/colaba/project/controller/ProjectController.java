package com.example.colaba.project.controller;

import com.example.colaba.project.service.ProjectService;
import com.example.colaba.project.service.TagService;
import com.example.colaba.shared.controller.BaseController;
import com.example.colaba.shared.dto.project.CreateProjectRequest;
import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.dto.project.ProjectScrollResponse;
import com.example.colaba.shared.dto.project.UpdateProjectRequest;
import com.example.colaba.shared.dto.tag.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "API for managing projects, including creation, updates, owner changes, and retrieval")
public class ProjectController extends BaseController {

    private final ProjectService projectService;
    private final TagService tagService;

    @PostMapping
    @Operation(summary = "Create a new project", description = "Creates a new project with the provided name, description, and owner ID. Validates for unique name and existing owner.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate project name"),
            @ApiResponse(responseCode = "404", description = "Owner user not found")
    })
    public ResponseEntity<Void> create(@Valid @RequestBody CreateProjectRequest request) {
        projectService.createProject(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project", description = "Partially updates a project by ID (name or description). Validates for duplicate names and applies changes only if different.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate project name"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse updated = projectService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/owner")
    @Operation(summary = "Change project owner", description = "Changes the owner of a project to a new user by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project owner changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (missing ownerId)"),
            @ApiResponse(responseCode = "404", description = "Project or new owner not found")
    })
    public ResponseEntity<ProjectResponse> changeOwner(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) {
        Long newOwnerId = request.get("ownerId");
        if (newOwnerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        ProjectResponse updated = projectService.changeProjectOwner(id, newOwnerId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResponse> getById(@PathVariable("id") Long id) {
        ProjectResponse response = projectService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all projects (non-paginated)", description = "Retrieves all projects without pagination.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all projects")
    })
    public ResponseEntity<List<ProjectResponse>> getAll() {
        List<ProjectResponse> list = projectService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Get projects by owner ID", description = "Retrieves a list of projects owned by a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of projects for the owner"),
            @ApiResponse(responseCode = "404", description = "Owner user not found")
    })
    public ResponseEntity<List<ProjectResponse>> getByOwner(@PathVariable("ownerId") Long ownerId) {
        List<ProjectResponse> list = projectService.getByOwnerId(ownerId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/scroll")
    @Operation(summary = "Get projects with scrolling pagination", description = "Retrieves projects for scrolling pagination (page and size). Parameters page/size are optional with defaults. Size capped at 50.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scroll response with projects, hasNext, and total"),
            @ApiResponse(responseCode = "400", description = "Invalid page size > 50")
    })
    public ResponseEntity<ProjectScrollResponse> scroll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        if (size > 50) size = 50;
        ProjectScrollResponse scroll = projectService.scroll(page, size);
        return ResponseEntity.ok(scroll);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project", description = "Deletes a project by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "Get tags by project ID with pagination", description = "Retrieves a paginated list of tags for a specific project. Supports standard Spring Pageable parameters. TODO: Move to project controller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tags for the project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Page<TagResponse>> getTagsByProject(
            @PathVariable Long id, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TagResponse> tags = tagService.getTagsByProject(id, pageable);
        return ResponseEntity.ok(tags);
    }
}
