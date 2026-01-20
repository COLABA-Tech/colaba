package com.example.colaba.project.unit;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.entity.projectmember.ProjectMemberId;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.mapper.ProjectMemberMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.service.ProjectMemberService;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.projectmember.DuplicateProjectMemberException;
import com.example.colaba.shared.common.exception.projectmember.ProjectMemberNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webflux.circuit.UserServiceClientWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserServiceClientWrapper userServiceClient;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private CreateProjectMemberRequest createRequest;
    private UpdateProjectMemberRequest updateRequest;
    private ProjectMemberJpa savedMember;
    private ProjectMemberResponse memberResponse;

    private final Long testProjectId = 1L;
    private final Long testUserId = 2L;
    private final ProjectRole testRole = ProjectRole.VIEWER;
    private ProjectJpa testProject;

    @BeforeEach
    void setUp() {
        testProject = ProjectJpa.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        savedMember = ProjectMemberJpa.builder()
                .projectId(testProjectId)
                .userId(testUserId)
                .role(testRole)
                .joinedAt(OffsetDateTime.now())
                .build();

        memberResponse = new ProjectMemberResponse(
                testProjectId,
                testUserId,
                testRole.getValue(),
                OffsetDateTime.now()
        );

        createRequest = new CreateProjectMemberRequest(testUserId, testRole);
        updateRequest = new UpdateProjectMemberRequest(ProjectRole.OWNER);

        // Мок TransactionTemplate
        when(transactionTemplate.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        doAnswer(invocation -> {
            Consumer<TransactionStatus> consumer = invocation.getArgument(0);
            consumer.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(Consumer.class));
    }

    @Test
    void getMembersByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectMemberJpa> mockPage = new PageImpl<>(List.of(savedMember));
        Page<ProjectMemberResponse> mockResponsePage = new PageImpl<>(List.of(memberResponse));

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(projectMemberRepository.findByProjectId(testProjectId, pageable)).thenReturn(mockPage);
        when(projectMemberMapper.toProjectMemberResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberService.getMembersByProject(testProjectId, pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().projectId().equals(testProjectId))
                .verifyComplete();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(projectMemberRepository).findByProjectId(testProjectId, pageable);
        verify(projectMemberMapper).toProjectMemberResponsePage(mockPage);
    }

    @Test
    void getMembersByProject_projectNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(projectService.getProjectEntityById(testProjectId))
                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberService.getMembersByProject(testProjectId, pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(projectMemberRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void createMembership_success_withRole() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectMemberRepository.existsById(id)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMemberMapper.toProjectMemberResponse(any(ProjectMemberJpa.class))).thenReturn(memberResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.projectId().equals(testProjectId) &&
                                response.userId().equals(testUserId) &&
                                response.role().equals(testRole.getValue()))
                .verifyComplete();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(userServiceClient).userExists(testUserId);
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository).save(argThat(member ->
                testProjectId.equals(member.getProjectId()) &&
                        testUserId.equals(member.getUserId()) &&
                        testRole.equals(member.getRole())));
        verify(projectMemberMapper).toProjectMemberResponse(any(ProjectMemberJpa.class));
    }

    @Test
    void createMembership_success_withDefaultRole() {
        // Given
        CreateProjectMemberRequest defaultRequest = new CreateProjectMemberRequest(testUserId, null);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        ProjectRole defaultRole = ProjectRole.getDefault();

        ProjectMemberResponse defaultResponse = new ProjectMemberResponse(
                testProjectId,
                testUserId,
                defaultRole.getValue(),
                OffsetDateTime.now()
        );

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectMemberRepository.existsById(id)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMemberMapper.toProjectMemberResponse(any(ProjectMemberJpa.class))).thenReturn(defaultResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, defaultRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.role().equals(defaultRole.getValue()))
                .verifyComplete();

        verify(projectMemberRepository).save(argThat(member -> defaultRole.equals(member.getRole())));
    }

    @Test
    void createMembership_duplicate_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(true));
        when(projectMemberRepository.existsById(id)).thenReturn(true);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateProjectMemberException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)) &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(userServiceClient).userExists(testUserId);
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMemberJpa.class));
    }

    @Test
    void createMembership_projectNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId))
                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(userServiceClient, never()).userExists(anyLong());
        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
    }

    @Test
    void createMembership_userNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(userServiceClient.userExists(testUserId)).thenReturn(Mono.just(false));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(userServiceClient).userExists(testUserId);
        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
    }

    @Test
    void updateMembership_success_withRoleChange() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        ProjectRole newRole = ProjectRole.OWNER;
        UpdateProjectMemberRequest changeRequest = new UpdateProjectMemberRequest(newRole);

        ProjectMemberResponse updatedResponse = new ProjectMemberResponse(
                testProjectId,
                testUserId,
                newRole.getValue(),
                OffsetDateTime.now()
        );

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberRepository.save(any(ProjectMemberJpa.class))).thenAnswer(i -> i.getArgument(0));
        when(projectMemberMapper.toProjectMemberResponse(any(ProjectMemberJpa.class))).thenReturn(updatedResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, changeRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.role().equals(newRole.getValue()))
                .verifyComplete();

        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository).save(argThat(member -> newRole.equals(member.getRole())));
        verify(projectMemberMapper).toProjectMemberResponse(any(ProjectMemberJpa.class));
    }

    @Test
    void updateMembership_noChange_returnsUnchanged() {
        // Given
        UpdateProjectMemberRequest noChangeRequest = new UpdateProjectMemberRequest(testRole);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, noChangeRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.role().equals(testRole.getValue()))
                .verifyComplete();

        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMemberJpa.class));
        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
    }

    @Test
    void updateMembership_nullRole_ignoresAndReturnsUnchanged() {
        // Given
        UpdateProjectMemberRequest nullRequest = new UpdateProjectMemberRequest(null);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, nullRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.role().equals(testRole.getValue()))
                .verifyComplete();

        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMemberJpa.class));
        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
    }

    @Test
    void updateMembership_notFound_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, updateRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectMemberNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)) &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMemberJpa.class));
    }

    @Test
    void deleteMembership_success() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.existsById(id)).thenReturn(true);

        // When
        Mono<Void> resultMono = projectMemberService.deleteMembership(testProjectId, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository).deleteById(id);
    }

    @Test
    void deleteMembership_notFound_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.existsById(id)).thenReturn(false);

        // When
        Mono<Void> resultMono = projectMemberService.deleteMembership(testProjectId, testUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectMemberNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)) &&
                                throwable.getMessage().contains(String.valueOf(testUserId)))
                .verify();

        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository, never()).deleteById(any(ProjectMemberId.class));
    }

    @Test
    void updateMembership_saveNotCalledWhenNoChanges() {
        // Given
        UpdateProjectMemberRequest sameRoleRequest = new UpdateProjectMemberRequest(testRole);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, sameRoleRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextCount(1)
                .verifyComplete();

        verify(projectMemberRepository, never()).save(any(ProjectMemberJpa.class));
    }
}