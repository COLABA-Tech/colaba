package com.example.colaba.unit.service;

import com.example.colaba.dto.tag.CreateTagRequest;
import com.example.colaba.dto.tag.TagResponse;
import com.example.colaba.dto.tag.UpdateTagRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.Tag;
import com.example.colaba.entity.task.Task;
import com.example.colaba.exception.tag.DuplicateTagException;
import com.example.colaba.exception.tag.TagNotFoundException;
import com.example.colaba.exception.task.TaskNotFoundException;
import com.example.colaba.mapper.TagMapper;
import com.example.colaba.repository.TagRepository;
import com.example.colaba.service.ProjectService;
import com.example.colaba.service.TagService;
import com.example.colaba.service.TaskService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskService taskService;

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
                .tags(new java.util.HashSet<>())
                .build();

        savedTag = Tag.builder()
                .id(testTagId)
                .name(testName)
                .project(testProject)
                .tasks(new java.util.HashSet<>())
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
        Page<TagResponse> result = tagService.getAllTags(pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(tagRepository).findAll(pageable);
        verify(tagMapper).toTagResponsePage(mockPage);
    }

    @Test
    void getTagById_success() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        TagResponse result = tagService.getTagById(testTagId);

        // Then
        assertEquals(testTagId, result.id());
        assertEquals(testName, result.name());
        verify(tagRepository).findById(testTagId);
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void getTagById_notFound_throwsException() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> tagService.getTagById(testTagId));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(tagMapper, never()).toTagResponse(any(Tag.class));
    }

    @Test
    void getTagsByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tag> mockPage = new PageImpl<>(List.of(savedTag));
        Page<TagResponse> mockResponsePage = new PageImpl<>(List.of(tagResponse));

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(tagRepository.findByProject(testProject, pageable)).thenReturn(mockPage);
        when(tagMapper.toTagResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Page<TagResponse> result = tagService.getTagsByProject(testProjectId, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProject(testProject, pageable);
        verify(tagMapper).toTagResponsePage(mockPage);
    }

    @Test
    void getTagsByProject_projectNotFound_throwsException() {
        // TODO
//        // Given
//        Pageable pageable = PageRequest.of(0, 10);
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenThrow(new ProjectNotFoundException(testProjectId));
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> tagService.getTagsByProject(testProjectId, pageable));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(tagRepository, never()).findByProject(any(Project.class), any(Pageable.class));
    }

    @Test
    void getTagsByTask_success() {
        // Given
        when(tagRepository.findByTaskId(testTaskId)).thenReturn(List.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        List<TagResponse> result = tagService.getTagsByTask(testTaskId);

        // Then
        assertEquals(1, result.size());
        assertEquals(testTagId, result.getFirst().id());
        verify(tagRepository).findByTaskId(testTaskId);
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void getTagsByTask_noTags_returnsEmptyList() {
        // Given
        when(tagRepository.findByTaskId(testTaskId)).thenReturn(List.of());

        // When
        List<TagResponse> result = tagService.getTagsByTask(testTaskId);

        // Then
        assertTrue(result.isEmpty());
        verify(tagRepository).findByTaskId(testTaskId);
        verifyNoInteractions(tagMapper);
    }

    @Test
    void createTag_success() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        TagResponse result = tagService.createTag(createRequest);

        // Then
        assertEquals(testTagId, result.id());
        assertEquals(testName, result.name());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, testName);
        verify(tagRepository).save(argThat(tag -> testName.equals(tag.getName())));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void createTag_duplicateName_throwsException() {
        // Given
        when(projectService.getProjectEntityById(testProjectId)).thenReturn(testProject);
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.of(savedTag));

        // When & Then
        DuplicateTagException exception = assertThrows(DuplicateTagException.class,
                () -> tagService.createTag(createRequest));
        assertEquals("Tag 'Test Tag' already exists in project " + testProjectId, exception.getMessage());
        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, testName);
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void createTag_projectNotFound_throwsException() {
        // TODO
//        // Given
//        when(projectService.getProjectEntityById(testProjectId))
//                .thenThrow(new ProjectNotFoundException(testProjectId));
//
//        // When & Then
//        ProjectNotFoundException exception = assertThrows(ProjectNotFoundException.class,
//                () -> tagService.createTag(createRequest));
//        assertEquals("Project not found: ID " + testProjectId, exception.getMessage());
//        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
//        verify(tagRepository, never()).save(any(Tag.class));
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
        TagResponse result = tagService.updateTag(testTagId, changeRequest);

        // Then
        assertEquals(testTagId, result.id());
        assertEquals(newName, result.name());
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
        TagResponse result = tagService.updateTag(testTagId, noChangeRequest);

        // Then
        assertEquals(testTagId, result.id());
        assertEquals(testName, result.name());
        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void updateTag_duplicateName_throwsException() {
        // Given
        UpdateTagRequest duplicateRequest = new UpdateTagRequest("Duplicate Name");

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, "Duplicate Name")).thenReturn(Optional.of(savedTag));

        // When & Then
        DuplicateTagException exception = assertThrows(DuplicateTagException.class,
                () -> tagService.updateTag(testTagId, duplicateRequest));
        assertEquals("Tag 'Duplicate Name' already exists in project " + testProjectId, exception.getMessage());
        verify(tagRepository).findById(testTagId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, "Duplicate Name");
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_notFound_throwsException() {
        // Given
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> tagService.updateTag(testTagId, updateRequest));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void updateTag_nullName_ignoresAndReturnsUnchanged() {
        // Given: null name (no change)
        UpdateTagRequest nullNameRequest = new UpdateTagRequest(null);

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        TagResponse result = tagService.updateTag(testTagId, nullNameRequest);

        // Then
        assertEquals(testTagId, result.id());
        assertEquals(testName, result.name());
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
        tagService.deleteTag(testTagId);

        // Then
        verify(tagRepository).existsById(testTagId);
        verify(tagRepository).deleteById(testTagId);
    }

    @Test
    void deleteTag_notFound_throwsException() {
        // Given
        when(tagRepository.existsById(testTagId)).thenReturn(false);

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> tagService.deleteTag(testTagId));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(tagRepository, never()).deleteById(testTagId);
    }

    @Test
    void assignTagToTask_success_newAssignment() {
        // Given
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService).saveTask(argThat(task -> task.getTags().contains(savedTag)));
    }

    @Test
    void assignTagToTask_idempotent_noSaveOnDuplicate() {
        // Given: Pre-add tag to task for duplicate simulation
        testTask.getTags().add(savedTag);  // Simulate already assigned

        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        tagService.assignTagToTask(testTaskId, testTagId);

        // Then
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService, never()).saveTask(any(Task.class));  // No save on duplicate
    }

    @Test
    void assignTagToTask_projectMismatch_throwsException() {
        // Given: Mismatch project
        Project otherProject = Project.builder().id(20L).build();
        savedTag.setProject(otherProject);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> tagService.assignTagToTask(testTaskId, testTagId));
        assertEquals("Tag does not belong to task's project", exception.getMessage());
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService, never()).saveTask(any(Task.class));
    }

    @Test
    void assignTagToTask_tagNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> tagService.assignTagToTask(testTaskId, testTagId));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService, never()).saveTask(any(Task.class));
    }

    @Test
    void assignTagToTask_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId)).thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> tagService.assignTagToTask(testTaskId, testTagId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(tagRepository, never()).findById(anyLong());
        verify(taskService, never()).saveTask(any(Task.class));
    }

    @Test
    void removeTagFromTask_success() {
        // Given: Pre-add tag
        testTask.getTags().add(savedTag);

        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService).saveTask(argThat(task -> !task.getTags().contains(savedTag)));
    }

    @Test
    void removeTagFromTask_notAssigned_stillSaves() {
        // Given: No tag pre-added (simulate not assigned)
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When
        tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService).saveTask(any(Task.class));  // Saves even if not removed (per code)
    }

    @Test
    void removeTagFromTask_projectMismatch_throwsNoExceptionButSaves() {
        // Given: Mismatch (code doesn't check in remove—only in assign)
        Project otherProject = Project.builder().id(20L).build();
        savedTag.setProject(otherProject);
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));

        // When (no throw expected)
        tagService.removeTagFromTask(testTaskId, testTagId);

        // Then
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService).saveTask(any(Task.class));  // Proceeds anyway
    }

    @Test
    void removeTagFromTask_tagNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId)).thenReturn(testTask);
        when(tagRepository.findById(testTagId)).thenReturn(Optional.empty());

        // When & Then
        TagNotFoundException exception = assertThrows(TagNotFoundException.class,
                () -> tagService.removeTagFromTask(testTaskId, testTagId));
        assertEquals("Tag not found: ID " + testTagId, exception.getMessage());
        verify(taskService).getTaskEntityById(testTaskId);
        verify(tagRepository).findById(testTagId);
        verify(taskService, never()).saveTask(any(Task.class));
    }

    @Test
    void removeTagFromTask_taskNotFound_throwsException() {
        // Given
        when(taskService.getTaskEntityById(testTaskId)).thenThrow(new TaskNotFoundException(testTaskId));

        // When & Then
        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class,
                () -> tagService.removeTagFromTask(testTaskId, testTagId));
        assertEquals("Task not found: ID " + testTaskId, exception.getMessage());
        verify(tagRepository, never()).findById(anyLong());
        verify(taskService, never()).saveTask(any(Task.class));
    }
}