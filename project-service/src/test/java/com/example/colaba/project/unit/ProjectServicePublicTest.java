package com.example.colaba.project.unit;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.project.service.ProjectServicePublic;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServicePublicTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectAccessCheckerLocal projectAccessCheckerLocal;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ProjectServicePublic projectServicePublic;

    private ProjectJpa testProject;
    private ProjectResponse testProjectResponse;
    private final Long testId = 1L;
    private final Long currentUserId = 1L;
    private final Long otherUserId = 2L;
    private final String testProjectName = "Test Project";
    private final String testDescription = "Test Description";

    @BeforeEach
    void setUp() {
        testProject = ProjectJpa.builder()
                .id(testId)
                .name(testProjectName)
                .description(testDescription)
                .ownerId(currentUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        testProjectResponse = new ProjectResponse(
                testId,
                testProjectName,
                testDescription,
                currentUserId,
                OffsetDateTime.now()
        );
    }

    // ==================== createProject Tests ====================

    @Test
    void createProject_success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, currentUserId);

        when(projectService.createProject(request)).thenReturn(Mono.just(testProjectResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.createProject(request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProjectResponse)
                .verifyComplete();

        verify(projectService).createProject(request);
    }

    @Test
    void createProject_withDifferentOwnerId_throwsAccessDenied() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, otherUserId);

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.createProject(request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("You can only create projects for yourself"))
                .verify();

        verify(projectService, never()).createProject(any());
    }

    @Test
    void createProject_withSameUser_success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, currentUserId);

        when(projectService.createProject(request)).thenReturn(Mono.just(testProjectResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.createProject(request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProjectResponse)
                .verifyComplete();

        verify(projectService).createProject(request);
    }

    // ==================== getProjectById Tests ====================

    @Test
    void getProjectById_admin_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.getProjectById(testId)).thenReturn(Mono.just(testProjectResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.getProjectById(testId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProjectResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).getProjectById(testId);
        verify(projectAccessCheckerLocal, never()).requireAnyRoleMono(any(), any());
    }

    @Test
    @Disabled
    void getProjectById_nonAdmin_withAccess_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testId, currentUserId))
                .thenReturn(Mono.empty());
        when(projectService.getProjectById(testId)).thenReturn(Mono.just(testProjectResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.getProjectById(testId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProjectResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testId, currentUserId);
        verify(projectService).getProjectById(testId);
    }

    @Test
    void getProjectById_nonAdmin_withoutAccess_throwsException() {
        // Given
        String errorMessage = "You must be a member of this project";
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testId, currentUserId))
                .thenReturn(Mono.error(new AccessDeniedException(errorMessage)));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.getProjectById(testId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testId, currentUserId);
        verify(projectService, never()).getProjectById(any());
    }

    // ==================== getAllProjects Tests ====================

    @Test
    void getAllProjects_admin_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectJpa> adminPage = new PageImpl<>(List.of(testProject), pageable, 1);
        Page<ProjectResponse> adminResponsePage = new PageImpl<>(List.of(testProjectResponse), pageable, 1);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectRepository.findAll(pageable)).thenReturn(adminPage);
        when(projectMapper.toProjectResponsePage(adminPage)).thenReturn(adminResponsePage);

        // When
        Mono<Page<ProjectResponse>> resultMono = projectServicePublic.getAllProjects(pageable, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProjectResponse))
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectRepository).findAll(pageable);
        verify(projectMapper).toProjectResponsePage(adminPage);
        verify(projectRepository, never()).findUserProjects(any(), any());
    }

    @Test
    void getAllProjects_nonAdmin_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectJpa> userPage = new PageImpl<>(List.of(testProject), pageable, 1);
        Page<ProjectResponse> userResponsePage = new PageImpl<>(List.of(testProjectResponse), pageable, 1);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectRepository.findUserProjects(currentUserId, pageable)).thenReturn(userPage);
        when(projectMapper.toProjectResponsePage(userPage)).thenReturn(userResponsePage);

        // When
        Mono<Page<ProjectResponse>> resultMono = projectServicePublic.getAllProjects(pageable, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProjectResponse))
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectRepository).findUserProjects(currentUserId, pageable);
        verify(projectMapper).toProjectResponsePage(userPage);
    }

    @Test
    void getAllProjects_emptyPage_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectJpa> emptyPage = Page.empty();
        Page<ProjectResponse> emptyResponsePage = Page.empty();

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectRepository.findUserProjects(currentUserId, pageable)).thenReturn(emptyPage);
        when(projectMapper.toProjectResponsePage(emptyPage)).thenReturn(emptyResponsePage);

        // When
        Mono<Page<ProjectResponse>> resultMono = projectServicePublic.getAllProjects(pageable, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(Page::isEmpty)
                .verifyComplete();
    }

    // ==================== updateProject Tests ====================

    @Test
    void updateProject_admin_success() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "Updated Desc", null);
        ProjectResponse updatedResponse = new ProjectResponse(testId, "Updated Name", "Updated Desc", currentUserId, OffsetDateTime.now());

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.updateProject(testId, request)).thenReturn(Mono.just(updatedResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.updateProject(testId, request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(updatedResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).updateProject(testId, request);
        verify(projectAccessCheckerLocal, never()).requireOwnerMono(any(), any());
    }

    @Test
    @Disabled
    void updateProject_nonAdmin_owner_success() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", null, null);
        ProjectResponse updatedResponse = new ProjectResponse(testId, "Updated Name", testDescription, currentUserId, OffsetDateTime.now());

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.empty());
        when(projectService.updateProject(testId, request)).thenReturn(Mono.just(updatedResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.updateProject(testId, request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(updatedResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService).updateProject(testId, request);
    }

    @Test
    void updateProject_nonAdmin_notOwner_throwsException() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", null, null);
        String errorMessage = "Only the project OWNER can perform this action";

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.error(new AccessDeniedException(errorMessage)));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.updateProject(testId, request, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService, never()).updateProject(any(), any());
    }

    // ==================== changeProjectOwner Tests ====================

    @Test
    void changeProjectOwner_admin_success() {
        // Given
        Long newOwnerId = 3L;
        ProjectResponse updatedResponse = new ProjectResponse(testId, testProjectName, testDescription, newOwnerId, OffsetDateTime.now());

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.changeProjectOwner(testId, newOwnerId)).thenReturn(Mono.just(updatedResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.changeProjectOwner(testId, newOwnerId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(updatedResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).changeProjectOwner(testId, newOwnerId);
        verify(projectAccessCheckerLocal, never()).requireOwnerMono(any(), any());
    }

    @Test
    @Disabled
    void changeProjectOwner_nonAdmin_owner_success() {
        // Given
        Long newOwnerId = 3L;
        ProjectResponse updatedResponse = new ProjectResponse(testId, testProjectName, testDescription, newOwnerId, OffsetDateTime.now());

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.empty());
        when(projectService.changeProjectOwner(testId, newOwnerId)).thenReturn(Mono.just(updatedResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.changeProjectOwner(testId, newOwnerId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(updatedResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService).changeProjectOwner(testId, newOwnerId);
    }

    @Test
    void changeProjectOwner_nonAdmin_notOwner_throwsException() {
        // Given
        Long newOwnerId = 3L;
        String errorMessage = "Only the project OWNER can perform this action";

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.error(new AccessDeniedException(errorMessage)));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.changeProjectOwner(testId, newOwnerId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService, never()).changeProjectOwner(any(), any());
    }

    // ==================== deleteProject Tests ====================

    @Test
    void deleteProject_admin_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        doNothing().when(projectService).deleteProject(testId);

        // When & Then
        StepVerifier.create(projectServicePublic.deleteProject(testId, currentUserId))
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).deleteProject(testId);
        verify(projectAccessCheckerLocal, never()).requireOwnerMono(any(), any());
    }

    @Test
    void deleteProject_nonAdmin_owner_success() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.empty());
        doNothing().when(projectService).deleteProject(testId);

        // When & Then
        StepVerifier.create(projectServicePublic.deleteProject(testId, currentUserId))
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService).deleteProject(testId);
    }

    @Test
    void deleteProject_nonAdmin_notOwner_throwsException() {
        // Given
        String errorMessage = "Only the project OWNER can perform this action";
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireOwnerMono(testId, currentUserId))
                .thenReturn(Mono.error(new AccessDeniedException(errorMessage)));

        // When & Then
        StepVerifier.create(projectServicePublic.deleteProject(testId, currentUserId))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectAccessCheckerLocal).requireOwnerMono(testId, currentUserId);
        verify(projectService, never()).deleteProject(any());
    }

    // ==================== getProjectByOwnerId Tests ====================

    @Test
    void getProjectByOwnerId_admin_viewingOthers_success() {
        // Given
        List<ProjectResponse> projects = List.of(testProjectResponse);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.getProjectByOwnerId(otherUserId)).thenReturn(Mono.just(projects));

        // When
        Mono<List<ProjectResponse>> resultMono = projectServicePublic.getProjectByOwnerId(otherUserId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(projects)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).getProjectByOwnerId(otherUserId);
    }

    @Test
    void getProjectByOwnerId_nonAdmin_viewingOwn_success() {
        // Given
        List<ProjectResponse> projects = List.of(testProjectResponse);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectService.getProjectByOwnerId(currentUserId)).thenReturn(Mono.just(projects));

        // When
        Mono<List<ProjectResponse>> resultMono = projectServicePublic.getProjectByOwnerId(currentUserId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(projects)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).getProjectByOwnerId(currentUserId);
    }

    @Test
    void getProjectByOwnerId_nonAdmin_viewingOthers_throwsException() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));

        // When
        Mono<List<ProjectResponse>> resultMono = projectServicePublic.getProjectByOwnerId(otherUserId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("You can only view your own projects"))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService, never()).getProjectByOwnerId(any());
    }

    @Test
    void getProjectByOwnerId_admin_viewingOwn_success() {
        // Given
        List<ProjectResponse> projects = List.of(testProjectResponse);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.getProjectByOwnerId(currentUserId)).thenReturn(Mono.just(projects));

        // When
        Mono<List<ProjectResponse>> resultMono = projectServicePublic.getProjectByOwnerId(currentUserId, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(projects)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).getProjectByOwnerId(currentUserId);
    }

    // ==================== scroll Tests ====================

    @Test
    void scroll_admin_success() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectJpa> adminPage = new PageImpl<>(List.of(testProject), pageable, 15);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
        ProjectScrollResponse expectedResponse = new ProjectScrollResponse(projectResponses, true, 15L);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectRepository.findAll(pageable)).thenReturn(adminPage);
        when(projectMapper.toProjectResponseList(adminPage.getContent())).thenReturn(projectResponses);

        // When
        Mono<ProjectScrollResponse> resultMono = projectServicePublic.scroll(page, size, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.projects().equals(projectResponses) &&
                                response.hasMore() &&
                                response.total() == 15)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectRepository).findAll(pageable);
        verify(projectMapper).toProjectResponseList(adminPage.getContent());
        verify(projectRepository, never()).findUserProjects(any(), any());
    }

    @Test
    void scroll_nonAdmin_success() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectJpa> userPage = new PageImpl<>(List.of(testProject), pageable, 1);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
        ProjectScrollResponse expectedResponse = new ProjectScrollResponse(projectResponses, false, 1L);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectRepository.findUserProjects(currentUserId, pageable)).thenReturn(userPage);
        when(projectMapper.toProjectResponseList(userPage.getContent())).thenReturn(projectResponses);

        // When
        Mono<ProjectScrollResponse> resultMono = projectServicePublic.scroll(page, size, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.projects().equals(projectResponses) &&
                                !response.hasMore() &&
                                response.total() == 1)
                .verifyComplete();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectRepository).findUserProjects(currentUserId, pageable);
        verify(projectMapper).toProjectResponseList(userPage.getContent());
    }

    @Test
    void scroll_emptyResults_success() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectJpa> emptyPage = Page.empty();
        List<ProjectResponse> emptyList = List.of();
        ProjectScrollResponse expectedResponse = new ProjectScrollResponse(emptyList, false, 0L);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectRepository.findUserProjects(currentUserId, pageable)).thenReturn(emptyPage);
        when(projectMapper.toProjectResponseList(emptyPage.getContent())).thenReturn(emptyList);

        // When
        Mono<ProjectScrollResponse> resultMono = projectServicePublic.scroll(page, size, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.projects().isEmpty() &&
                                !response.hasMore() &&
                                response.total() == 0)
                .verifyComplete();
    }

    @Test
    void scroll_lastPage_noMoreResults() {
        // Given
        int page = 2;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectJpa> lastPage = new PageImpl<>(List.of(testProject), pageable, 25);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectRepository.findAll(pageable)).thenReturn(lastPage);
        when(projectMapper.toProjectResponseList(lastPage.getContent())).thenReturn(projectResponses);

        // When
        Mono<ProjectScrollResponse> resultMono = projectServicePublic.scroll(page, size, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.projects().size() == 1 &&
                                !response.hasMore())
                .verifyComplete();
    }

    // ==================== Edge Cases Tests ====================

    @Test
    void getProjectById_withNullCurrentUserId_handlesGracefully() {
        // Given
        Long nullUserId = null;
        when(userServiceClient.isAdmin(nullUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testId, nullUserId))
                .thenReturn(Mono.error(new AccessDeniedException("User not authenticated")));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.getProjectById(testId, nullUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void createProject_withNullCurrentUserId_throwsAccessDenied() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, otherUserId);
        Long nullUserId = null;

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.createProject(request, nullUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("You can only create projects for yourself"))
                .verify();

        verify(projectService, never()).createProject(any());
    }

    @Test
    void getAllProjects_withDifferentPageable_success() {
        // Given
        Pageable pageable = PageRequest.of(2, 5, Sort.by("name").ascending());
        Page<ProjectJpa> userPage = new PageImpl<>(List.of(testProject), pageable, 15);
        Page<ProjectResponse> userResponsePage = new PageImpl<>(List.of(testProjectResponse), pageable, 15);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(false));
        when(projectRepository.findUserProjects(currentUserId, pageable)).thenReturn(userPage);
        when(projectMapper.toProjectResponsePage(userPage)).thenReturn(userResponsePage);

        // When
        Mono<Page<ProjectResponse>> resultMono = projectServicePublic.getAllProjects(pageable, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(userResponsePage)
                .verifyComplete();

        verify(projectRepository).findUserProjects(currentUserId, pageable);
    }

    @Test
    void updateProject_withEmptyRequest_admin_success() {
        // Given
        UpdateProjectRequest emptyRequest = new UpdateProjectRequest(null, null, null);

        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        when(projectService.updateProject(testId, emptyRequest)).thenReturn(Mono.just(testProjectResponse));

        // When
        Mono<ProjectResponse> resultMono = projectServicePublic.updateProject(testId, emptyRequest, currentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProjectResponse)
                .verifyComplete();
    }

    @Test
    void deleteProject_withServiceException_propagatesException() {
        // Given
        when(userServiceClient.isAdmin(currentUserId)).thenReturn(Mono.just(true));
        doThrow(new RuntimeException("Database error")).when(projectService).deleteProject(testId);

        // When & Then
        StepVerifier.create(projectServicePublic.deleteProject(testId, currentUserId))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Database error"))
                .verify();

        verify(userServiceClient).isAdmin(currentUserId);
        verify(projectService).deleteProject(testId);
    }
}