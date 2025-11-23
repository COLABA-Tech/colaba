package com.example.colaba.unit.service;

import com.example.colaba.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import com.example.colaba.entity.projectmember.ProjectMember;
import com.example.colaba.entity.projectmember.ProjectMemberId;
import com.example.colaba.entity.projectmember.ProjectRole;
import com.example.colaba.exception.project.ProjectNotFoundException;
import com.example.colaba.exception.projectmember.DuplicateProjectMemberException;
import com.example.colaba.exception.projectmember.ProjectMemberNotFoundException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.mapper.ProjectMemberMapper;
import com.example.colaba.repository.ProjectMemberRepository;
import com.example.colaba.service.ProjectMemberService;
import com.example.colaba.service.ProjectService;
import com.example.colaba.service.UserService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private ProjectMemberService projectMemberService;

    private CreateProjectMemberRequest createRequest;
    private UpdateProjectMemberRequest updateRequest;
    private ProjectMember savedMember;
    private ProjectMemberResponse memberResponse;

    private final Long testProjectId = 1L;
    private final Long testUserId = 2L;
    private final String testUsername = "testuser";
    private final ProjectRole testRole = ProjectRole.VIEWER;
    private Project testProject;
    private User testUser;

    @BeforeEach
    void setUp() {
        testProject = Project.builder().id(testProjectId).name("Test Project").build();
        testUser = User.builder().id(testUserId).username(testUsername).build();

        savedMember = ProjectMember.builder()
                .project(testProject)
                .user(testUser)
                .role(testRole)
                .joinedAt(LocalDateTime.now())
                .build();

        memberResponse = new ProjectMemberResponse(
                testProjectId, testProject.getName(), testUserId, testUsername, testRole.getValue(), LocalDateTime.now()
        );

        createRequest = new CreateProjectMemberRequest(testUserId, testRole);
        updateRequest = new UpdateProjectMemberRequest(ProjectRole.OWNER);
    }

    @Test
    void getMembersByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProjectMember> mockPage = new PageImpl<>(List.of(savedMember));
        Page<ProjectMemberResponse> mockResponsePage = new PageImpl<>(List.of(memberResponse));

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(projectMemberRepository.findByProjectId(testProjectId, pageable)).thenReturn(mockPage);
        when(projectMemberMapper.toProjectMemberResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<ProjectMemberResponse> result = projectMemberService.getMembersByProject(testProjectId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(projectMemberRepository).findByProjectId(testProjectId, pageable);
        verify(projectMemberMapper).toProjectMemberResponsePage(mockPage);
    }

    @Test
    void getMembersByProject_projectNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(projectService.getProjectEntityById(testProjectId))
                .thenThrow(new ProjectNotFoundException(testProjectId));

        // When & Then
        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
                () -> projectMemberService.getMembersByProject(testProjectId, pageable));
        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
        verify(projectMemberRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void createMembership_success_withRole() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testUserId)).thenReturn(testUser);
        when(projectMemberRepository.existsById(id)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(savedMember);
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        ProjectMemberResponse result = projectMemberService.createMembership(testProjectId, createRequest);

        // Then
        assertEquals(testProjectId, result.projectId());
        assertEquals(testUserId, result.userId());
        assertEquals(testRole.getValue(), result.role());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(userService).getUserEntityById(testUserId);
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository).save(argThat(member ->
                testProject.equals(member.getProject()) &&
                        testUser.equals(member.getUser()) &&
                        testRole.equals(member.getRole())));
        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
    }

    @Test
    void createMembership_success_withDefaultRole() {
        // Given: Null role in request
        CreateProjectMemberRequest defaultRequest = new CreateProjectMemberRequest(testUserId, null);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        ProjectRole defaultRole = ProjectRole.getDefault();
        ProjectMember defaultMember = ProjectMember.builder()
                .project(testProject)
                .user(testUser)
                .role(defaultRole)
                .build();

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testUserId)).thenReturn(testUser);
        when(projectMemberRepository.existsById(id)).thenReturn(false);
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(savedMember);
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        ProjectMemberResponse result = projectMemberService.createMembership(testProjectId, defaultRequest);

        // Then
        assertEquals(defaultRole.getValue(), result.role());  // Default applied
        verify(projectMemberRepository).save(argThat(member -> defaultRole.equals(member.getRole())));
    }

    @Test
    void createMembership_duplicate_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testUserId)).thenReturn(testUser);
        when(projectMemberRepository.existsById(id)).thenReturn(true);

        // When & Then
        DuplicateProjectMemberException exception = assertThrows(DuplicateProjectMemberException.class,
                () -> projectMemberService.createMembership(testProjectId, createRequest));
        assertEquals("User 'testuser' is already a member of project 1", exception.getMessage());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(userService).getUserEntityById(testUserId);
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void createMembership_projectNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId))
                .thenThrow(new ProjectNotFoundException(testProjectId));

        // When & Then
        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
                () -> projectMemberService.createMembership(testProjectId, createRequest));
        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
        verify(userService, never()).getUserEntityById(anyLong());
        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
    }

    @Test
    void createMembership_userNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(userService.getUserEntityById(testUserId))
                .thenThrow(new UserNotFoundException(testUserId));

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> projectMemberService.createMembership(testProjectId, createRequest));
        assertEquals("User not found: ID " + testUserId, exception.getMessage());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(userService).getUserEntityById(testUserId);
        verify(projectMemberRepository, never()).existsById(any(ProjectMemberId.class));
    }

    @Test
    void updateMembership_success_withRoleChange() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        ProjectRole newRole = ProjectRole.OWNER;
        UpdateProjectMemberRequest changeRequest = new UpdateProjectMemberRequest(newRole);
        ProjectMember updatedMember = ProjectMember.builder()
                .project(testProject)
                .user(testUser)
                .role(newRole)
                .build();
        ProjectMemberResponse updatedResponse = new ProjectMemberResponse(
                testProjectId, testProject.getName(), testUserId, testUsername, newRole.getValue(), LocalDateTime.now()
        );

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(updatedMember);
        when(projectMemberMapper.toProjectMemberResponse(updatedMember)).thenReturn(updatedResponse);

        // When
        ProjectMemberResponse result = projectMemberService.updateMembership(testProjectId, testUserId, changeRequest);

        // Then
        assertEquals(newRole.getValue(), result.role());
        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository).save(argThat(member -> newRole.equals(member.getRole())));
        verify(projectMemberMapper).toProjectMemberResponse(updatedMember);
    }

    @Test
    void updateMembership_noChange_returnsUnchanged() {
        // Given: Same role
        UpdateProjectMemberRequest noChangeRequest = new UpdateProjectMemberRequest(testRole);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        ProjectMemberResponse result = projectMemberService.updateMembership(testProjectId, testUserId, noChangeRequest);

        // Then
        assertEquals(testRole.getValue(), result.role());  // unchanged
        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
    }

    @Test
    void updateMembership_nullRole_ignoresAndReturnsUnchanged() {
        // Given: Null role (no change)
        UpdateProjectMemberRequest nullRequest = new UpdateProjectMemberRequest(null);
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);

        when(projectMemberRepository.findById(id)).thenReturn(Optional.of(savedMember));
        when(projectMemberMapper.toProjectMemberResponse(savedMember)).thenReturn(memberResponse);

        // When
        ProjectMemberResponse result = projectMemberService.updateMembership(testProjectId, testUserId, nullRequest);

        // Then
        assertEquals(testRole.getValue(), result.role());  // unchanged
        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
        verify(projectMemberMapper).toProjectMemberResponse(savedMember);
    }

    @Test
    void updateMembership_notFound_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        ProjectMemberNotFoundException exception = assertThrows(ProjectMemberNotFoundException.class,
                () -> projectMemberService.updateMembership(testProjectId, testUserId, updateRequest));
        assertEquals("Project member not found: project ID 1, user ID 2", exception.getMessage());
        verify(projectMemberRepository).findById(id);
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void deleteMembership_success() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.existsById(id)).thenReturn(true);
        doNothing().when(projectMemberRepository).deleteById(id);

        // When
        projectMemberService.deleteMembership(testProjectId, testUserId);

        // Then
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository).deleteById(id);
    }

    @Test
    void deleteMembership_notFound_throwsException() {
        // Given
        ProjectMemberId id = new ProjectMemberId(testProjectId, testUserId);
        when(projectMemberRepository.existsById(id)).thenReturn(false);

        // When & Then
        ProjectMemberNotFoundException exception = assertThrows(ProjectMemberNotFoundException.class,
                () -> projectMemberService.deleteMembership(testProjectId, testUserId));
        assertEquals("Project member not found: project ID 1, user ID 2", exception.getMessage());
        verify(projectMemberRepository).existsById(id);
        verify(projectMemberRepository, never()).deleteById(id);
    }
}