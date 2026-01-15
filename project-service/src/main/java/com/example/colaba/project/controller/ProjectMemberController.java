package com.example.colaba.project.controller;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.service.ProjectMemberService;
import com.example.colaba.shared.common.controller.BaseController;
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
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Members", description = "API for managing project members and their roles")
public class ProjectMemberController extends BaseController {
    private final ProjectMemberService projectMemberService;

    @GetMapping
    @Operation(summary = "Get project members with pagination", description = "Retrieves a paginated list of all members for a specific project. Supports standard Spring Pageable parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of project members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEntity<Page<ProjectMemberResponse>>> getMembersByProject(
            @PathVariable Long projectId,
            Pageable pageable) {
        pageable = validatePageable(pageable);
        return projectMemberService.getMembersByProject(projectId, pageable)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    @Operation(summary = "Add member to project", description = "Adds a new member to the project with the specified role. Validates for duplicate membership and existing user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added to project successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate membership"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    public Mono<ResponseEntity<ProjectMemberResponse>> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectMemberRequest request) {
        return projectMemberService.createMembership(projectId, request)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update member role", description = "Updates the role of an existing project member. Only changes the role if different from current.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member role updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Project member not found")
    })
    public Mono<ResponseEntity<ProjectMemberResponse>> updateMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProjectMemberRequest request) {
        return projectMemberService.updateMembership(projectId, userId, request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Remove member from project", description = "Removes a member from the project. Validates that the membership exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Member removed from project successfully"),
            @ApiResponse(responseCode = "404", description = "Project member not found")
    })
    public Mono<ResponseEntity<Void>> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        return projectMemberService.deleteMembership(projectId, userId)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }
}
