package com.example.colaba.project.unit;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
import com.example.colaba.shared.webflux.circuit.UserServiceClientWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private UserServiceClientWrapper userServiceClient;

    @Mock
    private TaskServiceClientWrapper taskServiceClient;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private ProjectJpa testProject;
    private ProjectResponse testProjectResponse;
    private final Long testId = 1L;
    private final Long testUserId = 1L;
    private final String testProjectName = "Test Project";
    private final String testDescription = "Test Description";

    @BeforeEach
    void setUp() {
        testProject = ProjectJpa.builder()
                .id(testId)
                .name(testProjectName)
                .description(testDescription)
                .ownerId(testUserId)
                .createdAt(OffsetDateTime.now())
                .build();

        testProjectResponse = new ProjectResponse(
                testId,
                testProjectName,
                testDescription,
                testUserId,
                OffsetDateTime.now()
        );
    }

    @Test
    void createProject_success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);

        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
        when(userServiceClient.userExists(testUserId)).thenReturn(true);
        when(projectRepository.save(any(ProjectJpa.class))).thenReturn(testProject);
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testId) &&
                                response.name().equals(testProjectName) &&
                                response.ownerId().equals(testUserId))
                .verifyComplete();

        verify(projectRepository).existsByName(testProjectName);
        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository).save(any(ProjectJpa.class));
        verify(projectMemberRepository).save(any()); // Verify project member is saved
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void createProject_duplicateName_throwsException() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);

        when(projectRepository.existsByName(testProjectName)).thenReturn(true);

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateProjectNameException &&
                                throwable.getMessage().contains(testProjectName))
                .verify();

        verify(projectRepository).existsByName(testProjectName);
        verify(userServiceClient, never()).userExists(anyLong());
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void createProject_userNotFound_throwsException() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);

        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
        when(userServiceClient.userExists(testUserId)).thenReturn(false);

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(projectRepository).existsByName(testProjectName);
        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void getProjectById_success() {
        // Given
        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.getProjectById(testId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testId) &&
                                response.name().equals(testProjectName))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void getProjectById_notFound_throwsException() {
        // Given
        when(projectRepository.findById(testId)).thenReturn(Optional.empty());

        // When
        Mono<ProjectResponse> resultMono = projectService.getProjectById(testId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testId)))
                .verify();

        verify(projectRepository).findById(testId);
        verify(projectMapper, never()).toProjectResponse(any(ProjectJpa.class));
    }

    @Test
    void getProjectEntityById_success() {
        // Given
        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));

        // When
        Mono<ProjectJpa> resultMono = projectService.getProjectEntityById(testId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testProject)
                .verifyComplete();

        verify(projectRepository).findById(testId);
    }

//    @Test
//    void getAllProjects_success() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<ProjectJpa> mockPage = new PageImpl<>(List.of(testProject));
//        Page<ProjectResponse> mockResponsePage = new PageImpl<>(List.of(testProjectResponse));
//
//        when(projectRepository.findAll(pageable)).thenReturn(mockPage);
//        when(projectMapper.toProjectResponsePage(mockPage)).thenReturn(mockResponsePage);
//
//        // When
//        Mono<Page<ProjectResponse>> resultMono = projectService.getAllProjects(pageable);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(page ->
//                        page.getContent().size() == 1 &&
//                                page.getContent().get(0).id().equals(testId)
//                )
//                .verifyComplete();
//
//        verify(projectRepository).findAll(pageable);
//        verify(projectMapper).toProjectResponsePage(mockPage);
//    }

    @Test
    void updateProject_success() {
        // Given
        String updatedName = "Updated Project Name";
        String updatedDescription = "Updated Description";
        UpdateProjectRequest request = new UpdateProjectRequest(updatedName, updatedDescription, null);

        ProjectJpa updatedProject = ProjectJpa.builder()
                .id(testId)
                .name(updatedName)
                .description(updatedDescription)
                .ownerId(testUserId)
                .build();

        ProjectResponse updatedResponse = new ProjectResponse(
                testId, updatedName, updatedDescription, testUserId, OffsetDateTime.now()
        );

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectRepository.existsByNameAndIdNot(updatedName, testId)).thenReturn(false);
        when(projectRepository.save(any(ProjectJpa.class))).thenReturn(updatedProject);
        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.name().equals(updatedName) &&
                                response.description().equals(updatedDescription))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(projectRepository).existsByNameAndIdNot(updatedName, testId);
        verify(projectRepository).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(updatedProject);
    }

    @Test
    void updateProject_duplicateName_throwsException() {
        // Given
        String duplicateName = "Duplicate Project";
        UpdateProjectRequest request = new UpdateProjectRequest(duplicateName, null, null);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectRepository.existsByNameAndIdNot(duplicateName, testId)).thenReturn(true);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateProjectNameException &&
                                throwable.getMessage().contains(duplicateName))
                .verify();

        verify(projectRepository).findById(testId);
        verify(projectRepository).existsByNameAndIdNot(duplicateName, testId);
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void updateProject_changeOwner_success() {
        // Given
        Long newOwnerId = 2L;
        UpdateProjectRequest request = new UpdateProjectRequest(null, null, newOwnerId);

        ProjectJpa updatedProject = ProjectJpa.builder()
                .id(testId)
                .name(testProjectName)
                .description(testDescription)
                .ownerId(newOwnerId)
                .build();

        ProjectResponse updatedResponse = new ProjectResponse(
                testId, testProjectName, testDescription, newOwnerId, OffsetDateTime.now()
        );

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(true);
        when(projectRepository.save(any(ProjectJpa.class))).thenReturn(updatedProject);
        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.ownerId().equals(newOwnerId))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(userServiceClient).userExists(newOwnerId);
        verify(projectRepository).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(updatedProject);
    }

    @Test
    void updateProject_newOwnerNotFound_throwsException() {
        // Given
        Long newOwnerId = 2L;
        UpdateProjectRequest request = new UpdateProjectRequest(null, null, newOwnerId);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(false);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(newOwnerId)))
                .verify();

        verify(projectRepository).findById(testId);
        verify(userServiceClient).userExists(newOwnerId);
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void updateProject_noChanges_returnsUnchangedProject() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest(null, null, null);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.name().equals(testProjectName) &&
                                response.description().equals(testDescription))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(projectRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(projectRepository, never()).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void updateProject_blankName_ignored() {
        // Given
        UpdateProjectRequest request = new UpdateProjectRequest(" ", null, null);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.name().equals(testProjectName))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(projectRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
        verify(projectRepository, never()).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void changeProjectOwner_success() {
        // Given
        Long newOwnerId = 2L;

        ProjectJpa updatedProject = ProjectJpa.builder()
                .id(testId)
                .name(testProjectName)
                .description(testDescription)
                .ownerId(newOwnerId)
                .build();

        ProjectResponse updatedResponse = new ProjectResponse(
                testId, testProjectName, testDescription, newOwnerId, OffsetDateTime.now()
        );

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(true);
        when(projectRepository.save(any(ProjectJpa.class))).thenReturn(updatedProject);
        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.changeProjectOwner(testId, newOwnerId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.ownerId().equals(newOwnerId))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(userServiceClient).userExists(newOwnerId);
        verify(projectRepository).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(updatedProject);
    }

    @Test
    void changeProjectOwner_sameOwner_returnsUnchanged() {
        // Given
        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.changeProjectOwner(testId, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.ownerId().equals(testUserId))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(userServiceClient, never()).userExists(anyLong());
        verify(projectRepository, never()).save(any(ProjectJpa.class));
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void deleteProject_success() {
        // Given
        when(projectRepository.existsById(testId)).thenReturn(true);

        // When
        projectService.deleteProject(testId);

        // Then
        verify(projectRepository).existsById(testId);
        verify(projectMemberRepository).deleteByProjectId(testId);
        verify(tagRepository).deleteByProjectId(testId);
        verify(taskServiceClient).deleteTasksByProject(testId);
        verify(projectRepository).deleteById(testId);
    }

    @Test
    void deleteProject_notFound_throwsException() {
        // Given
        when(projectRepository.existsById(testId)).thenReturn(false);

        // When & Then
        ProjectNotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.deleteProject(testId)
        );
        assertEquals("Project not found: ID " + testId, exception.getMessage());

        verify(projectRepository).existsById(testId);
        verify(projectMemberRepository, never()).deleteByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void getProjectByOwnerId_success() {
        // Given
        List<ProjectJpa> projects = List.of(testProject);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);

        when(userServiceClient.userExists(testUserId)).thenReturn(true);
        when(projectRepository.findByOwnerId(testUserId)).thenReturn(projects);
        when(projectMapper.toProjectResponseList(projects)).thenReturn(projectResponses);

        // When
        Mono<List<ProjectResponse>> resultMono = projectService.getProjectByOwnerId(testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(list ->
                        list.size() == 1 &&
                                list.get(0).id().equals(testId))
                .verifyComplete();

        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository).findByOwnerId(testUserId);
        verify(projectMapper).toProjectResponseList(projects);
    }

    @Test
    void getProjectByOwnerId_userNotFound_throwsException() {
        // Given
        when(userServiceClient.userExists(testUserId)).thenReturn(false);

        // When
        Mono<List<ProjectResponse>> resultMono = projectService.getProjectByOwnerId(testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository, never()).findByOwnerId(anyLong());
    }

//    @Test
//    void scroll_success() {
//        // Given
//        int page = 0;
//        int size = 10;
//        Pageable pageable = PageRequest.of(page, size);
//        Page<ProjectJpa> projectPage = new PageImpl<>(List.of(testProject), pageable, 1);
//        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
//
//        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
//        when(projectMapper.toProjectResponseList(projectPage.getContent())).thenReturn(projectResponses);
//
//        // When
//        Mono<ProjectScrollResponse> resultMono = projectService.scroll(page, size);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.projects().size() == 1 &&
//                                !response.hasMore() &&
//                                response.total() == 1)
//                .verifyComplete();
//
//        verify(projectRepository).findAll(pageable);
//        verify(projectMapper).toProjectResponseList(projectPage.getContent());
//    }

//    @Test
//    void scroll_withNextPage() {
//        // Given
//        int page = 0;
//        int size = 1;
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<ProjectJpa> projectPage = new PageImpl<>(List.of(testProject), pageable, 2);
//        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
//
//        when(projectRepository.findAll(pageable)).thenReturn(projectPage);
//        when(projectMapper.toProjectResponseList(projectPage.getContent())).thenReturn(projectResponses);
//
//        // When
//        Mono<ProjectScrollResponse> resultMono = projectService.scroll(page, size);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.projects().size() == 1 &&
//                                response.hasMore() &&
//                                response.total() == 2)
//                .verifyComplete();
//
//        verify(projectRepository).findAll(pageable);
//        verify(projectMapper).toProjectResponseList(projectPage.getContent());
//    }

    @Test
    void handleUserDeletion_success() {
        // Given
        Long userId = 1L;

        // When
        projectService.handleUserDeletion(userId).block();

        // Then
        verify(projectMemberRepository).deleteByUserId(userId);
    }
}