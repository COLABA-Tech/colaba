//package com.example.colaba.project.unit;
//
//import com.example.colaba.project.repository.ProjectRepository;
//import com.example.colaba.project.service.ProjectService;
//import com.example.colaba.shared.client.UserServiceClient;
//import com.example.colaba.shared.dto.project.CreateProjectRequest;
//import com.example.colaba.shared.dto.project.ProjectResponse;
//import com.example.colaba.shared.dto.project.ProjectScrollResponse;
//import com.example.colaba.shared.dto.project.UpdateProjectRequest;
//import com.example.colaba.shared.entity.Project;
//import com.example.colaba.user.entity.User;
//import com.example.colaba.user.entity.UserJpa;
//import com.example.colaba.shared.exception.project.DuplicateProjectNameException;
//import com.example.colaba.shared.exception.project.ProjectNotFoundException;
//import com.example.colaba.shared.exception.user.UserNotFoundException;
//import com.example.colaba.project.mapper.ProjectMapper;
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
//class ProjectServiceTest {
//
//    @Mock
//    private ProjectRepository projectRepository;
//
//    @Mock
//    private UserServiceClient userServiceClient;
//
//    @Mock
//    private ProjectMapper projectMapper;
//
//    @Mock
//    private UserMapper userMapper;
//
//    @InjectMocks
//    private ProjectService projectService;
//
//    private User testUser;
//    private UserJpa testUserJpa;
//    private Project testProject;
//    private ProjectResponse testProjectResponse;
//    private final Long testId = 1L;
//    private final Long testUserId = 1L;
//    private final String testProjectName = "Test Project";
//    private final String testDescription = "Test Description";
//
//    @BeforeEach
//    void setUp() {
//        testUser = User.builder()
//                .id(testUserId)
//                .username("testuser")
//                .email("test@example.com")
//                .build();
//
//        testUserJpa = UserJpa.builder()
//                .id(testUserId)
//                .username("testuser")
//                .email("test@example.com")
//                .build();
//
//        testProject = Project.builder()
//                .id(testId)
//                .name(testProjectName)
//                .description(testDescription)
//                .owner(testUserJpa)
//                .createdAt(OffsetDateTime.now())
//                .build();
//
//        testProjectResponse = new ProjectResponse(
//                testId,
//                testProjectName,
//                testDescription,
//                testUserId,
//                "testuser",
//                OffsetDateTime.now()
//        );
//    }
//
//    @Test
//    void createProject_success() {
//        // Given
//        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);
//
//        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
//        when(userServiceClient.getUserEntityById(testUserId)).thenReturn(testUser);
//        when(userMapper.toUserJpa(testUser)).thenReturn(testUserJpa);
//        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
//        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.createProject(request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.id().equals(testId) &&
//                                response.name().equals(testProjectName) &&
//                                response.ownerId().equals(testUserId))
//                .verifyComplete();
//
//        verify(projectRepository).existsByName(testProjectName);
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(projectRepository).save(any(Project.class));
//        verify(projectMapper).toProjectResponse(testProject);
//    }
//
//    @Test
//    void createProject_duplicateName_throwsException() {
//        // Given
//        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);
//
//        when(projectRepository.existsByName(testProjectName)).thenReturn(true);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.createProject(request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof DuplicateProjectNameException &&
//                                throwable.getMessage().contains(testProjectName))
//                .verify();
//
//        verify(projectRepository).existsByName(testProjectName);
//        verify(userServiceClient, never()).getUserEntityById(anyLong());
//        verify(projectRepository, never()).save(any(Project.class));
//    }
//
//    @Test
//    void createProject_userNotFound_throwsException() {
//        // Given
//        CreateProjectRequest request = new CreateProjectRequest(testProjectName, testDescription, testUserId);
//
//        when(projectRepository.existsByName(testProjectName)).thenReturn(false);
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(userServiceClient.getUserEntityById(testUserId)).thenThrow(feignException);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.createProject(request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof UserNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testUserId)))
//                .verify();
//
//        verify(projectRepository).existsByName(testProjectName);
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(projectRepository, never()).save(any(Project.class));
//    }
//
//    @Test
//    void getProjectById_success() {
//        // Given
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.getProjectById(testId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.id().equals(testId) &&
//                                response.name().equals(testProjectName))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(projectMapper).toProjectResponse(testProject);
//    }
//
//    @Test
//    void getProjectById_notFound_throwsException() {
//        // Given
//        when(projectRepository.findById(testId)).thenReturn(Optional.empty());
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.getProjectById(testId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testId)))
//                .verify();
//
//        verify(projectRepository).findById(testId);
//        verify(projectMapper, never()).toProjectResponse(any(Project.class));
//    }
//
//    @Test
//    void getProjectEntityById_success() {
//        // Given
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//
//        // When
//        Mono<Project> resultMono = projectService.getProjectEntityById(testId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNext(testProject)
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//    }
//
//    @Test
//    void getAllProjects_success() {
//        // Given
//        List<Project> projects = List.of(testProject);
//        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
//
//        when(projectRepository.findAll()).thenReturn(projects);
//        when(projectMapper.toProjectResponseList(projects)).thenReturn(projectResponses);
//
//        // When
//        Mono<List<ProjectResponse>> resultMono = projectService.getAllProjects();
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(list ->
//                        list.size() == 1 &&
//                                list.get(0).id().equals(testId))
//                .verifyComplete();
//
//        verify(projectRepository).findAll();
//        verify(projectMapper).toProjectResponseList(projects);
//    }
//
//    @Test
//    void updateProject_success() {
//        // Given
//        String updatedName = "Updated Project Name";
//        String updatedDescription = "Updated Description";
//        UpdateProjectRequest request = new UpdateProjectRequest(updatedName, updatedDescription, null);
//
//        Project updatedProject = Project.builder()
//                .id(testId)
//                .name(updatedName)
//                .description(updatedDescription)
//                .owner(testUserJpa)
//                .build();
//
//        ProjectResponse updatedResponse = new ProjectResponse(
//                testId, updatedName, updatedDescription, testUserId, "testuser", OffsetDateTime.now()
//        );
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(projectRepository.existsByNameAndIdNot(updatedName, testId)).thenReturn(false);
//        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
//        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.name().equals(updatedName) &&
//                                response.description().equals(updatedDescription))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(projectRepository).existsByNameAndIdNot(updatedName, testId);
//        verify(projectRepository).save(any(Project.class));
//        verify(projectMapper).toProjectResponse(updatedProject);
//    }
//
//    @Test
//    void updateProject_duplicateName_throwsException() {
//        // Given
//        String duplicateName = "Duplicate Project";
//        UpdateProjectRequest request = new UpdateProjectRequest(duplicateName, null, null);
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(projectRepository.existsByNameAndIdNot(duplicateName, testId)).thenReturn(true);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof DuplicateProjectNameException &&
//                                throwable.getMessage().contains(duplicateName))
//                .verify();
//
//        verify(projectRepository).findById(testId);
//        verify(projectRepository).existsByNameAndIdNot(duplicateName, testId);
//        verify(projectRepository, never()).save(any(Project.class));
//    }
//
//    @Test
//    void updateProject_changeOwner_success() {
//        // Given
//        Long newOwnerId = 2L;
//        User newOwner = User.builder().id(newOwnerId).username("newowner").build();
//        UserJpa newOwnerJpa = UserJpa.builder().id(newOwnerId).username("newowner").build();
//        UpdateProjectRequest request = new UpdateProjectRequest(null, null, newOwnerId);
//
//        Project updatedProject = Project.builder()
//                .id(testId)
//                .name(testProjectName)
//                .description(testDescription)
//                .owner(newOwnerJpa)
//                .build();
//
//        ProjectResponse updatedResponse = new ProjectResponse(
//                testId, testProjectName, testDescription, newOwnerId, "newowner", OffsetDateTime.now()
//        );
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(userServiceClient.getUserEntityById(newOwnerId)).thenReturn(newOwner);
//        when(userMapper.toUserJpa(newOwner)).thenReturn(newOwnerJpa);
//        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
//        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.ownerId().equals(newOwnerId) &&
//                                response.ownerName().equals("newowner"))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(userServiceClient).getUserEntityById(newOwnerId);
//        verify(userMapper).toUserJpa(newOwner);
//        verify(projectRepository).save(any(Project.class));
//    }
//
//    @Test
//    void updateProject_noChanges_returnsUnchangedProject() {
//        // Given
//        UpdateProjectRequest request = new UpdateProjectRequest(null, null, null);
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.name().equals(testProjectName) &&
//                                response.description().equals(testDescription))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(projectRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
//        verify(projectRepository, never()).save(any(Project.class));
//        verify(projectMapper).toProjectResponse(testProject);
//    }
//
//    @Test
//    void updateProject_blankName_ignored() {
//        // Given
//        UpdateProjectRequest request = new UpdateProjectRequest(" ", null, null);
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(projectMapper.toProjectResponse(testProject)).thenReturn(testProjectResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.updateProject(testId, request);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.name().equals(testProjectName))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(projectRepository, never()).existsByNameAndIdNot(anyString(), anyLong());
//        verify(projectRepository, never()).save(any(Project.class));
//    }
//
//    @Test
//    void changeProjectOwner_success() {
//        // Given
//        Long newOwnerId = 2L;
//        User newOwner = User.builder().id(newOwnerId).username("newowner").build();
//        UserJpa newOwnerJpa = UserJpa.builder().id(newOwnerId).username("newowner").build();
//
//        Project updatedProject = Project.builder()
//                .id(testId)
//                .name(testProjectName)
//                .description(testDescription)
//                .owner(newOwnerJpa)
//                .build();
//
//        ProjectResponse updatedResponse = new ProjectResponse(
//                testId, testProjectName, testDescription, newOwnerId, "newowner", OffsetDateTime.now()
//        );
//
//        when(projectRepository.findById(testId)).thenReturn(Optional.of(testProject));
//        when(userServiceClient.getUserEntityById(newOwnerId)).thenReturn(newOwner);
//        when(userMapper.toUserJpa(newOwner)).thenReturn(newOwnerJpa);
//        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);
//        when(projectMapper.toProjectResponse(updatedProject)).thenReturn(updatedResponse);
//
//        // When
//        Mono<ProjectResponse> resultMono = projectService.changeProjectOwner(testId, newOwnerId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(response ->
//                        response.ownerId().equals(newOwnerId))
//                .verifyComplete();
//
//        verify(projectRepository).findById(testId);
//        verify(userServiceClient).getUserEntityById(newOwnerId);
//        verify(userMapper).toUserJpa(newOwner);
//        verify(projectRepository).save(any(Project.class));
//    }
//
//    @Test
//    void deleteProject_success() {
//        // Given
//        when(projectRepository.existsById(testId)).thenReturn(true);
//        doNothing().when(projectRepository).deleteById(testId);
//
//        // When
//        Mono<Void> resultMono = projectService.deleteProject(testId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .verifyComplete();
//
//        verify(projectRepository).existsById(testId);
//        verify(projectRepository).deleteById(testId);
//    }
//
//    @Test
//    void deleteProject_notFound_throwsException() {
//        // Given
//        when(projectRepository.existsById(testId)).thenReturn(false);
//
//        // When
//        Mono<Void> resultMono = projectService.deleteProject(testId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof ProjectNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testId)))
//                .verify();
//
//        verify(projectRepository).existsById(testId);
//        verify(projectRepository, never()).deleteById(anyLong());
//    }
//
//    @Test
//    void getProjectByOwnerId_success() {
//        // Given
//        List<Project> projects = List.of(testProject);
//        List<ProjectResponse> projectResponses = List.of(testProjectResponse);
//
//        when(userServiceClient.getUserEntityById(testUserId)).thenReturn(testUser);
//        when(userMapper.toUserJpa(testUser)).thenReturn(testUserJpa);
//        when(projectRepository.findByOwner(testUserJpa)).thenReturn(projects);
//        when(projectMapper.toProjectResponseList(projects)).thenReturn(projectResponses);
//
//        // When
//        Mono<List<ProjectResponse>> resultMono = projectService.getProjectByOwnerId(testUserId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectNextMatches(list ->
//                        list.size() == 1 &&
//                                list.get(0).id().equals(testId))
//                .verifyComplete();
//
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(userMapper).toUserJpa(testUser);
//        verify(projectRepository).findByOwner(testUserJpa);
//        verify(projectMapper).toProjectResponseList(projects);
//    }
//
//    @Test
//    void getProjectByOwnerId_userNotFound_throwsException() {
//        // Given
//        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
//        when(userServiceClient.getUserEntityById(testUserId)).thenThrow(feignException);
//
//        // When
//        Mono<List<ProjectResponse>> resultMono = projectService.getProjectByOwnerId(testUserId);
//
//        // Then
//        StepVerifier.create(resultMono)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof UserNotFoundException &&
//                                throwable.getMessage().contains(String.valueOf(testUserId)))
//                .verify();
//
//        verify(userServiceClient).getUserEntityById(testUserId);
//        verify(projectRepository, never()).findByOwner(any(UserJpa.class));
//    }
//
//    @Test
//    void scroll_success() {
//        // Given
//        int page = 0;
//        int size = 10;
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Project> projectPage = new PageImpl<>(List.of(testProject), pageable, 1);
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
//
//    @Test
//    void scroll_withNextPage() {
//        // Given
//        int page = 0;
//        int size = 1;
//        Pageable pageable = PageRequest.of(page, size);
//
//        Project project2 = Project.builder()
//                .id(2L)
//                .name("Project 2")
//                .owner(testUserJpa)
//                .build();
//
//        Page<Project> projectPage = new PageImpl<>(List.of(testProject), pageable, 2);
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
//}
