//package com.example.colaba.project.unit;
//
//import com.example.colaba.project.repository.ProjectMemberRepository;
//import com.example.colaba.project.service.ProjectMemberService;
//import com.example.colaba.project.service.ProjectService;
//import com.example.colaba.shared.client.UserServiceClient;
//import com.example.colaba.shared.dto.projectmember.CreateProjectMemberRequest;
//import com.example.colaba.shared.dto.projectmember.ProjectMemberResponse;
//import com.example.colaba.shared.dto.projectmember.UpdateProjectMemberRequest;
//import com.example.colaba.shared.entity.Project;
//import com.example.colaba.shared.entity.User;
//import com.example.colaba.shared.entity.UserJpa;
//import com.example.colaba.shared.entity.projectmember.ProjectMember;
//import com.example.colaba.shared.entity.projectmember.ProjectMemberId;
//import com.example.colaba.shared.entity.projectmember.ProjectRole;
//import com.example.colaba.shared.exception.project.ProjectNotFoundException;
//import com.example.colaba.shared.exception.projectmember.DuplicateProjectMemberException;
//import com.example.colaba.shared.exception.projectmember.ProjectMemberNotFoundException;
//import com.example.colaba.shared.exception.user.UserNotFoundException;
//import com.example.colaba.project.mapper.ProjectMemberMapper;
//import com.example.colaba.user.mapper.UserMapper;
//import feign.FeignException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProjectMemberServiceTest {
//
//    @Mock
//    private ProjectMemberRepository projectMemberRepository;
//
//    @Mock
//    private ProjectService projectService;
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private ProjectMemberMapper projectMemberMapper;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private ProjectMemberService projectMemberService;
//
//    private CreateProjectMemberRequest createRequest;
//    private UpdateProjectMemberRequest updateRequest;
//    private ProjectMember savedMember;
//    private ProjectMemberResponse memberResponse;
//
//    private final Long testProjectId = 1L;
//    private final Long testUserId = 2L;
//    private final String testUsername = "testuser";
//    private final ProjectRole testRole = ProjectRole.VIEWER;
//    private Project testProject;
//    private User testUser;
//    private UserJpa testUserJpa;
//
//    @BeforeEach
//    void setUp() {
//        testProject = Project.builder()
//                .id(testProjectId)
//                .name("Test Project")
//                .build();
//
//        testUser = User.builder()
//                .id(testUserId)
//                .username(testUsername)
//                .build();
//
//        testUserJpa = UserJpa.builder()
//                .id(testUserId)
//                .username(testUsername)
//                .email("test@example.com")
//                .build();
//
//        savedMember = ProjectMember.builder()
//                .projectId(testProjectId)
//                .userId(testUserId)
//                .project(testProject)
//                .user(testUserJpa)
//                .role(testRole)
//                .joinedAt(OffsetDateTime.now())
//                .build();
//
//        memberResponse = new ProjectMemberResponse(
//                testProjectId, testProject.getName(), testUserId, testUsername,
//                testRole.getValue(), OffsetDateTime.now()
//        );
//
//        createRequest = new CreateProjectMemberRequest(testUserId, testRole);
//        updateRequest = new UpdateProjectMemberRequest(ProjectRole.OWNER);
//    }
//
//    @Test
//    void getMembersByProject_success() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        Page<ProjectMember> mockPage = new PageImpl<>(List.of(savedMember));
//        Page<ProjectMemberResponse> mockResponsePage = new PageImpl<>(List.of(memberResponse));
//
//        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
//        when(projectMemberRepository.findByProjectId(testProjectId, pageable)).thenReturn(mockPage);
//        when(projectMemberMapper.toProjectMemberResponsePage(mockPage)).thenReturn(mockResponsePage);
//
//        // When
//        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberService.getMembersByProject(testProjectId, pageable);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(page ->
//                        page.getContent().size() == 1 &&
//                                page.getContent().get(0).projectId().equals(testProjectId))
//                .verifyComplete();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(projectMemberRepository).findByProjectId(testProjectId, pageable);
//        verify(projectMemberMapper).toProjectMemberResponsePage(mockPage);
//    }
//
//    @Test
//    void getMembersByProject_projectNotFound_throwsException() {
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));
//
//        // When
//        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberService.getMembersByProject(testProjectId, pageable);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testProjectId)))
//                .verify();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(projectMemberRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
//    }
//
//    @Test
//    void createMembership_success_withRole() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//
//        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
//        when(userServiceClient.getUserEntityById(testUserId)).thenReturn(testUser);
//        when(userMapper.toUserJpa(testUser)).thenReturn(testUserJpa);
//        when(projectMemberRepository.existsById(id)).thenReturn(false);
//        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(savedMember);
//        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.projectId().equals(testProjectId) &&
//                                response.userId().equals(testUserId) &&
//                                response.role().equals(testRole.getValue()))
//                .verifyComplete();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(userMapper).toUserJpa(testUser);
//        verify(projectMemberRepository).existsById(id);
//        verify(projectMemberRepository).save(argThat(member ->
//                testProjectId.equals(member.getProjectId()) &&
//                        testUserId.equals(member.getUserId()) &&
//                        testRole.equals(member.getRole())));
//        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
//    }
//
//    @Test
//    void createMembership_success_withDefaultRole() {
//        // Given
//        CreateProjectMemberRequest defaultRequest = new CreateProjectMemberRequest(testUserId, null);
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//        ProjectRole defaultRole = ProjectRole.getDefault();
//
//        ProjectMember defaultMember = ProjectMember.builder()
//                .projectId(testProjectId)
//                .userId(testUserId)
//                .project(testProject)
//                .user(testUserJpa)
//                .role(defaultRole)
//                .build();
//
//        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
//        when(userServiceClient.getUserEntityById(testUserId)).thenReturn(testUser);
//        when(userMapper.toUserJpa(testUser)).thenReturn(testUserJpa);
//        when(projectMemberRepository.existsById(id)).thenReturn(false);
//        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(defaultMember);
//        when(projectMemberMapper.toProjectMemberResponse(defaultMember)).thenReturn(memberResponse);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, defaultRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.role().equals(defaultRole.getValue()))
//                .verifyComplete();
//
//        verify(projectMemberRepository).save(argThat(member -> defaultRole.equals(member.getRole())));
//    }
//
//    @Test
//    void createMembership_duplicate_throwsException() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//
//        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
//        when(userServiceClient.getUserEntityById(testUserId)).thenReturn(testUser);
//        when(userMapper.toUserJpa(testUser)).thenReturn(testUserJpa);
//        when(projectMemberRepository.existsById(id)).thenReturn(true);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof DuplicateProjectMemberException &&
//                                throwable.getMessage().contains(testUsername))
//                .verify();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(userMapper).toUserJpa(testUser);
//        verify(projectMemberRepository).existsById(id);
//        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
//    }
//
//    @Test
//    void createMembership_projectNotFound_throwsException() {
//        // Given
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testProjectId)))
//                .verify();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(userServiceClient, never()).getUserEntityById(anyLong());
//        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
//    }
//
//    @Test
//    void createMembership_userNotFound_throwsException() {
//        // Given
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//
//        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
//        when(userServiceClient.getUserEntityById(testUserId)).thenThrow(feignException);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.createMembership(testProjectId, createRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof UserNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testUserId)))
//                .verify();
//
//        verify(projectService).getProjectEntityById(testProjectId);
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
//    }
//
//    @Test
//    void updateMembership_success_withRoleChange() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//        ProjectRole newRole = ProjectRole.OWNER;
//        UpdateProjectMemberRequest changeRequest = new UpdateProjectMemberRequest(newRole);
//
//        ProjectMember updatedMember = ProjectMember.builder()
//                .projectId(testProjectId)
//                .userId(testUserId)
//                .project(testProject)
//                .user(testUserJpa)
//                .role(newRole)
//                .build();
//
//        ProjectMemberResponse updatedResponse = new ProjectMemberResponse(
//                testProjectId, testProject.getName(), testUserId, testUsername,
//                newRole.getValue(), OffsetDateTime.now()
//        );
//
//        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
//        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(updatedMember);
//        when(projectMemberMapper.toProjectMemberResponse(updatedMember)).thenReturn(updatedResponse);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, changeRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.role().equals(newRole.getValue()))
//                .verifyComplete();
//
//        verify(projectMemberRepository).findById(id);
//        verify(projectMemberRepository).save(argThat(member -> newRole.equals(member.getRole())));
//        verify(projectMemberMapper).toProjectMemberResponse(updatedMember);
//    }
//
//    @Test
//    void updateMembership_noChange_returnsUnchanged() {
//        // Given
//        UpdateProjectMemberRequest noChangeRequest = new UpdateProjectMemberRequest(testRole);
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//
//        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
//        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, noChangeRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.role().equals(testRole.getValue()))
//                .verifyComplete();
//
//        verify(projectMemberRepository).findById(id);
//        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
//        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
//    }
//
//    @Test
//    void updateMembership_nullRole_ignoresAndReturnsUnchanged() {
//        // Given
//        UpdateProjectMemberRequest nullRequest = new UpdateProjectMemberRequest(null);
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//
//        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
//        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, nullRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.role().equals(testRole.getValue()))
//                .verifyComplete();
//
//        verify(projectMemberRepository).findById(id);
//        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
//        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
//    }
//
//    @Test
//    void updateMembership_notFound_throwsException() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//        when(projectMemberRepository.findById(id)).thenReturn(Optional.empty());
//
//        // When
//        Mono<ProjectMemberResponse> resultMono = projectMemberService.updateMembership(testProjectId, testUserId, updateRequest);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectMemberNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testProjectId)) &&
//                                throwable.getMessage().contains(String.valueOf(testUserId)))
//                .verify();
//
//        verify(projectMemberRepository).findById(id);
//        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
//    }
//
//    @Test
//    void deleteMembership_success() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//        when(projectMemberRepository.existsById(id)).thenReturn(true);
//        doNothing().when(projectMemberRepository).deleteById(id);
//
//        // When
//        Mono<Void> resultMono = projectMemberService.deleteMembership(testProjectId, testUserId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .verifyComplete();
//
//        verify(projectMemberRepository).existsById(id);
//        verify(projectMemberRepository).deleteById(id);
//    }
//
//    @Test
//    void deleteMembership_notFound_throwsException() {
//        // Given
//        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
//        when(projectMemberRepository.existsById(id)).thenReturn(false);
//
//        // When
//        Mono<Void> resultMono = projectMemberService.deleteMembership(testProjectId, testUserId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectMemberNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testProjectId)) &&
//                                throwable.getMessage().contains(String.valueOf(testUserId)))
//                .verify();
//
//        verify(projectMemberRepository).existsById(id);
//        verify(projectMemberRepository, never()).deleteById(any(ProjectMemberId.class));
//    }
//}