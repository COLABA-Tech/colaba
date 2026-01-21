package com.example.colaba.project.unit;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.entity.ProjectRole;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Mock
    private TransactionTemplate transactionTemplate;

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
                testUserId
        );

        // Мок для execute(TransactionCallback<T>) — возвращает результат callback
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        // Мок для executeWithoutResult(Consumer<TransactionStatus>) — выполняет consumer
        doAnswer(invocation -> {
            Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(Consumer.class));

        // updateRole — void метод
        doNothing().when(projectMemberRepository).updateRole(anyLong(), anyLong(), any(ProjectRole.class));
    }

    @Test
    void createProject_success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription);

        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            p.setId(testId); // эмулируем сохранение с id
            return p;
        });
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request, testUserId);

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
        verify(projectMemberRepository).save(any(ProjectMemberJpa.class));
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
    }

    @Test
    void createProject_duplicateName_throwsException() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription);

        when(projectRepository.existsByName(testProjectName)).thenReturn(true);
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateProjectNameException &&
                                throwable.getMessage().contains(testProjectName))
                .verify();

        verify(projectRepository).existsByName(testProjectName);
        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void createProject_userNotFound_throwsException() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription);

        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(false));

        // When
        Mono<ProjectResponse> resultMono = projectService.createProject(request, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

//        verify(projectRepository).existsByName(testProjectName);
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

    @Test
    void updateProject_success() {
        // Given
        String updatedName = "Updated Project Name";
        String updatedDescription = "Updated Description";
        UpdateProjectRequest request = new UpdateProjectRequest(updatedName, updatedDescription, null);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(projectRepository.existsByNameAndIdNot(updatedName, testId)).thenReturn(false);
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa saved = i.getArgument(0);
            saved.setName(updatedName);
            saved.setDescription(updatedDescription);
            return saved;
        });
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getOwnerId());
        });

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
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
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

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(true));
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMemberRepository.existsByProjectIdAndUserId(testId, newOwnerId)).thenReturn(true);
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getOwnerId());
        });

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
        verify(projectMemberRepository).updateRole(testId, testUserId, ProjectRole.EDITOR);
        verify(projectMemberRepository).updateRole(testId, newOwnerId, ProjectRole.OWNER);
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
    }

    @Test
    void updateProject_newOwnerNotFound_throwsException() {
        // Given
        Long newOwnerId = 2L;
        UpdateProjectRequest request = new UpdateProjectRequest(null, null, newOwnerId);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(false));

        // When
        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(newOwnerId)))
                .verify();

//        verify(projectRepository).findById(testId);
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

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(true));
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            p.setOwnerId(newOwnerId);
            return p;
        });
        when(projectMemberRepository.existsByProjectIdAndUserId(testId, newOwnerId)).thenReturn(true);
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getOwnerId());
        });

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
        verify(projectMemberRepository).updateRole(testId, testUserId, ProjectRole.EDITOR);
        verify(projectMemberRepository).updateRole(testId, newOwnerId, ProjectRole.OWNER);
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
    }

    @Test
    void changeProjectOwner_sameOwner_returnsUnchanged() {
        // Given
        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);

        // When
        Mono<ProjectResponse> resultMono = projectService.changeProjectOwner(testId, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.ownerId().equals(testUserId))
                .verifyComplete();

        verify(projectRepository).findById(testId);
        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository, never()).save(any(ProjectJpa.class));
        verify(projectMemberRepository, never()).updateRole(anyLong(), anyLong(), any(ProjectRole.class));
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    void deleteProject_success() {
        // Given
        when(projectRepository.existsById(testId)).thenReturn(true);
        when(taskServiceClient.deleteTasksByProject(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectService.deleteProject(testId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(projectRepository).existsById(testId);
        verify(taskServiceClient).deleteTasksByProject(testId);
        verify(projectMemberRepository).deleteByProjectId(testId);
        verify(tagRepository).deleteByProjectId(testId);
        verify(projectRepository).deleteById(testId);
    }

    @Test
    void deleteProject_notFound_throwsException() {
        // Given
        when(projectRepository.existsById(testId)).thenReturn(false);

        // When
        Mono<Void> resultMono = projectService.deleteProject(testId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testId)))
                .verify();

        verify(projectRepository).existsById(testId);
        verify(taskServiceClient, never()).deleteTasksByProject(anyLong());
        verify(projectMemberRepository, never()).deleteByProjectId(anyLong());
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void getProjectsByOwnerId_success() {
        // Given
        List<ProjectJpa> projects = List.of(testProject);
        List<ProjectResponse> projectResponses = List.of(testProjectResponse);

        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectRepository.findByOwnerId(testUserId)).thenReturn(projects);
        when(projectMapper.toProjectResponseList(projects)).thenReturn(projectResponses);

        // When
        Mono<List<ProjectResponse>> resultMono = projectService.getProjectsByOwnerId(testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(list ->
                        list.size() == 1 &&
                                list.getFirst().id().equals(testId))
                .verifyComplete();

        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository).findByOwnerId(testUserId);
        verify(projectMapper).toProjectResponseList(projects);
    }

    @Test
    void getProjectsByOwnerId_userNotFound_throwsException() {
        // Given
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(false));

        // When
        Mono<List<ProjectResponse>> resultMono = projectService.getProjectsByOwnerId(testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(userServiceClient).userExists(testUserId);
        verify(projectRepository, never()).findByOwnerId(anyLong());
    }

    @Test
    void handleUserDeletion_success() {
        // Given
        Long userId = 1L;
        List<ProjectJpa> userProjects = List.of(testProject);

        when(projectRepository.findByOwnerId(userId)).thenReturn(userProjects);
        when(projectRepository.existsById(testId)).thenReturn(true);
        when(taskServiceClient.deleteTasksByProject(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectService.handleUserDeletion(userId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(projectRepository).findByOwnerId(userId);
        verify(taskServiceClient).deleteTasksByProject(testId);
        verify(projectMemberRepository).deleteByProjectId(testId);
        verify(tagRepository).deleteByProjectId(testId);
        verify(projectRepository).deleteById(testId);
        verify(projectMemberRepository).deleteByUserId(userId);
    }

    @Test
    void updateProject_changeOwner_newOwnerNotMember_createsNewMember() {
        // Given
        Long newOwnerId = 2L;
        UpdateProjectRequest request = new UpdateProjectRequest(null, null, newOwnerId);

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(true));
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMemberRepository.existsByProjectIdAndUserId(testId, newOwnerId)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getOwnerId());
        });

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
        verify(projectMemberRepository).existsByProjectIdAndUserId(testId, newOwnerId);
        verify(projectMemberRepository).updateRole(testId, testUserId, ProjectRole.EDITOR);
        verify(projectMemberRepository).save(argThat(member ->
                member.getProjectId().equals(testId) &&
                        member.getUserId().equals(newOwnerId) &&
                        member.getRole() == ProjectRole.OWNER));
        verify(projectMemberRepository, never()).updateRole(testId, newOwnerId, ProjectRole.OWNER);
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
    }

    @Test
    void changeProjectOwner_newOwnerNotFound_throwsException() {
        // Given
        Long newOwnerId = 2L;

        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(false));

        // When
        Mono<ProjectResponse> resultMono = projectService.changeProjectOwner(testId, newOwnerId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(newOwnerId)))
                .verify();

        verify(userServiceClient).userExists(newOwnerId);
        verify(projectRepository, never()).findById(anyLong());
        verify(projectRepository, never()).save(any(ProjectJpa.class));
    }

    @Test
    void changeProjectOwner_newOwnerNotMember_createsNewMember() {
        // Given
        Long newOwnerId = 2L;

        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
        when(userServiceClient.userExists(newOwnerId)).thenReturn(Mono.just(true));
        when(projectRepository.save(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            p.setOwnerId(newOwnerId);
            return p;
        });
        when(projectMemberRepository.existsByProjectIdAndUserId(testId, newOwnerId)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMapper.toProjectResponse(any(ProjectJpa.class))).thenAnswer(i -> {
            ProjectJpa p = i.getArgument(0);
            return new ProjectResponse(p.getId(), p.getName(), p.getDescription(), p.getOwnerId());
        });

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
        verify(projectMemberRepository).existsByProjectIdAndUserId(testId, newOwnerId);
        verify(projectMemberRepository).updateRole(testId, testUserId, ProjectRole.EDITOR);
        verify(projectMemberRepository).save(argThat(member ->
                member.getProjectId().equals(testId) &&
                        member.getUserId().equals(newOwnerId) &&
                        member.getRole() == ProjectRole.OWNER));
        verify(projectMemberRepository, never()).updateRole(testId, newOwnerId, ProjectRole.OWNER);
        verify(projectMapper).toProjectResponse(any(ProjectJpa.class));
    }
}