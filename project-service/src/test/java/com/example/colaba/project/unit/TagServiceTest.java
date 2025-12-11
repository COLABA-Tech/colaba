package com.example.colaba.project.unit;

import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.project.service.TagService;
import com.example.colaba.shared.client.TaskServiceClient;
import com.example.colaba.shared.dto.tag.CreateTagRequest;
import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.shared.dto.tag.UpdateTagRequest;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.Tag;
import com.example.colaba.shared.entity.task.Task;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.exception.tag.DuplicateTagException;
import com.example.colaba.shared.exception.tag.TagNotFoundException;
import com.example.colaba.shared.exception.task.TaskNotFoundException;
import com.example.colaba.shared.mapper.TagMapper;
import feign.FeignException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskServiceClient taskServiceClient;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private CreateTagRequest createRequest;
    private UpdateTagRequest updateRequest;
    private Tag savedTag;
    private TagResponse tagResponse;

    private final Long testTagId = 1L;
    private final Long testTaskId = 2L;
    private final String testName = "Test Tag";
    private final Long testProjectId = 10L;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        testTask = Task.builder()
                .id(testTaskId)
                .project(testProject)
                .tags(Set.of())
                .build();

        savedTag = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(Set.of())
                .build();

        tagResponse = new TagResponse(
                testTagId, testName, testProjectId, testProject.getName()
        );

        createRequest = new CreateTagRequest(testName, testProjectId);
        updateRequest = new UpdateTagRequest("Updated Name");
    }

    @Test
    void getAllTags_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> mockPage = new PageImpl<>(List.of(savedTag));
        Page<TagResponse> mockResponsePage = new PageImpl<>(List.of(tagResponse));

        when(tagRepository.findAll(pageable)).thenReturn(mockPage);
        when(tagMapper.toTagResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Mono<Page<TagResponse>> resultMono = tagService.getAllTags(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().get(0).id().equals(testTagId))
                .verifyComplete();

        verify(tagRepository).findAll(pageable);
        verify(tagMapper).toTagResponsePage(mockPage);
    }

    @Test
    void getTagById_success() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.getTagById(testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void getTagById_notFound_throwsException() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When
        Mono<TagResponse> resultMono = tagService.getTagById(testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TagNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTagId)))
                .verify();

        verify(tagRepository).findById(testTagId);
        verify(tagMapper, never()).toTagResponse(any(Tag.class));
    }

    @Test
    void getTagsByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> mockPage = new PageImpl<>(List.of(savedTag));
        Page<TagResponse> mockResponsePage = new PageImpl<>(List.of(tagResponse));

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(tagRepository.findByProject(testProject, pageable)).thenReturn(mockPage);
        when(tagMapper.toTagResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Mono<Page<TagResponse>> resultMono = tagService.getTagsByProject(testProjectId, pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().get(0).id().equals(testTagId))
                .verifyComplete();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProject(testProject, pageable);
        verify(tagMapper).toTagResponsePage(mockPage);
    }

    @Test
    void getTagsByProject_projectNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(projectService.getProjectEntityById(testProjectId))
                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));

        // When
        Mono<Page<TagResponse>> resultMono = tagService.getTagsByProject(testProjectId, pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository, never()).findByProject(any(Project.class), any(Pageable.class));
    }

    @Test
    void getTagsByTask_success() {
        // Given
        when(tagRepository.findByTaskId(testTaskId)).thenReturn(List.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<List<TagResponse>> resultMono = tagService.getTagsByTask(testTaskId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(list ->
                        list.size() == 1 &&
                                list.get(0).id().equals(testTagId))
                .verifyComplete();

        verify(tagRepository).findByTaskId(testTaskId);
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void getTagsByTask_noTags_returnsEmptyList() {
        // Given
        when(tagRepository.findByTaskId(testTaskId)).thenReturn(List.of());

        // When
        Mono<List<TagResponse>> resultMono = tagService.getTagsByTask(testTaskId);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(List::isEmpty)
                .verifyComplete();

        verify(tagRepository).findByTaskId(testTaskId);
        verify(tagMapper, never()).toTagResponse(any(Tag.class));
    }

    @Test
    void createTag_success() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.createTag(createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, testName);
        verify(tagRepository).save(argThat(tag -> testName.equals(tag.getName())));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void createTag_duplicateName_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(testProject));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.of(savedTag));

        // When
        Mono<TagResponse> resultMono = tagService.createTag(createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateTagException &&
                                throwable.getMessage().contains(testName))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, testName);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_projectNotFound_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId))
                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));

        // When
        Mono<TagResponse> resultMono = tagService.createTag(createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof ProjectNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_success_withChange() {
        // Given
        String newName = "Updated Tag";
        UpdateTagRequest changeRequest = new UpdateTagRequest(newName);
        Tag updatedTag = Tag.builder().id(testTagId).name(newName).project(testProject).build();
        TagResponse updatedResponse = new TagResponse(testTagId, newName, testProjectId, testProject.getName());

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, newName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(updatedTag);
        when(tagMapper.toTagResponse(updatedTag)).thenReturn(updatedResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, changeRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(newName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, newName);
        verify(tagRepository).save(argThat(tag -> newName.equals(tag.getName())));
        verify(tagMapper).toTagResponse(updatedTag);
    }

    @Test
    void updateTag_noChange_returnsUnchanged() {
        // Given
        UpdateTagRequest noChangeRequest = new UpdateTagRequest(testName);

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, noChangeRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void updateTag_duplicateName_throwsException() {
        // Given
        UpdateTagRequest duplicateRequest = new UpdateTagRequest("Duplicate Name");
        Tag duplicateTag = Tag.builder().id(2L).name("Duplicate Name").project(testProject).build();

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, "Duplicate Name")).thenReturn(Optional.of(duplicateTag));

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, duplicateRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateTagException &&
                                throwable.getMessage().contains("Duplicate Name"))
                .verify();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, "Duplicate Name");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_notFound_throwsException() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, updateRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TagNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTagId)))
                .verify();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_nullName_ignoresAndReturnsUnchanged() {
        // Given
        UpdateTagRequest nullNameRequest = new UpdateTagRequest(null);

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, nullNameRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void deleteTag_success() {
        // Given
        when(tagRepository.existsById(testTagId)).thenReturn(true);
        doNothing().when(tagRepository).deleteById(testTagId);

        // When
        Mono<Void> resultMono = tagService.deleteTag(testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(tagRepository).existsById(testTagId);
        verify(tagRepository).deleteById(testTagId);
    }

    @Test
    void deleteTag_notFound_throwsException() {
        // Given
        when(tagRepository.existsById(testTagId)).thenReturn(false);

        // When
        Mono<Void> resultMono = tagService.deleteTag(testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TagNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTagId)))
                .verify();

        verify(tagRepository).existsById(testTagId);
        verify(tagRepository, never()).deleteById(anyLong());
    }

    @Test
    void assignTagToTask_success_newAssignment() {
        // Given
        Task taskWithTags = Task.builder()
                .id(testTaskId)
                .project(testProject)
                .tags(new HashSet<>())
                .build();

        Tag tagWithTasks = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(new HashSet<>())
                .build();

        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(taskWithTags);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(tagWithTasks));
        when(taskServiceClient.updateTask(eq(testTaskId), any(Task.class))).thenReturn(taskWithTags);

        // When
        Mono<Void> resultMono = tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient).updateTask(eq(testTaskId), argThat(task ->
                task.getTags().contains(tagWithTasks)));
    }

    @Test
    void assignTagToTask_idempotent_noSaveOnDuplicate() {
        // Given
        Task taskWithTags = Task.builder()
                .id(testTaskId)
                .project(testProject)
                .tags(new HashSet<>())
                .build();

        Tag tagWithTasks = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(new HashSet<>())
                .build();

        taskWithTags.getTags().add(tagWithTasks);
        tagWithTasks.getTasks().add(taskWithTags);

        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(taskWithTags);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(tagWithTasks));

        // When
        Mono<Void> resultMono = tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void assignTagToTask_projectMismatch_throwsException() {
        // Given
        Project otherProject = Project.builder().id(20L).build();
        savedTag.setProject(otherProject);

        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        Mono<Void> resultMono = tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("Tag does not belong to task's project"))
                .verify();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void assignTagToTask_tagNotFound_throwsException() {
        // Given
        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenAnswer(_ -> Optional.empty());

        // When
        Mono<Void> resultMono = tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TagNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTagId)))
                .verify();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void assignTagToTask_taskNotFound_throwsException() {
        // Given
        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
        when(taskServiceClient.getTaskEntityById(testTaskId)).thenThrow(feignException);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        Mono<Void> resultMono = tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TaskNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTaskId)))
                .verify();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void removeTagFromTask_success() {
        // Given
        Task taskWithTags = Task.builder()
                .id(testTaskId)
                .project(testProject)
                .tags(new HashSet<>())
                .build();

        Tag tagWithTasks = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(new HashSet<>())
                .build();

        taskWithTags.getTags().add(tagWithTasks);
        tagWithTasks.getTasks().add(taskWithTags);

        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(taskWithTags);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(tagWithTasks));
        when(taskServiceClient.updateTask(eq(testTaskId), any(Task.class))).thenReturn(taskWithTags);

        // When
        Mono<Void> resultMono = tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient).updateTask(eq(testTaskId), argThat(task ->
                !task.getTags().contains(tagWithTasks)));
    }

    @Test
    void removeTagFromTask_notAssigned_stillSaves() {
        // Given
        Task taskWithTags = Task.builder()
                .id(testTaskId)
                .project(testProject)
                .tags(new HashSet<>())
                .build();

        Tag tagWithTasks = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(new HashSet<>())
                .build();

        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(taskWithTags);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(tagWithTasks));
        when(taskServiceClient.updateTask(eq(testTaskId), any(Task.class))).thenReturn(taskWithTags);

        // When
        Mono<Void> resultMono = tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient).updateTask(eq(testTaskId), any(Task.class));
    }

    @Test
    void removeTagFromTask_tagNotFound_throwsException() {
        // Given
        when(taskServiceClient.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When
        Mono<Void> resultMono = tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TagNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTagId)))
                .verify();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }

    @Test
    void removeTagFromTask_taskNotFound_throwsException() {
        // Given
        FeignException.NotFound feignException = mock(FeignException.NotFound.class);
        when(taskServiceClient.getTaskEntityById(testTaskId)).thenThrow(feignException);

        // When
        Mono<Void> resultMono = tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof TaskNotFoundException &&
                                throwable.getMessage().contains(String.valueOf(testTaskId)))
                .verify();

        verify(taskServiceClient).getTaskEntityById(testTaskId);
        verify(taskServiceClient, never()).updateTask(anyLong(), any(Task.class));
    }
}
