package com.example.colaba.project.unit;

import com.example.colaba.project.circuit.TaskServiceClientWrapper;
import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.project.service.ProjectService;
import com.example.colaba.project.service.TagService;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.tag.DuplicateTagException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskServiceClientWrapper taskServiceClient;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    private CreateTagRequest createRequest;
    private UpdateTagRequest updateRequest;
    private TagJpa savedTag;
    private TagResponse tagResponse;

    private final Long testTagId = 1L;
    private final String testName = "Test Tag";
    private final Long testProjectId = 10L;

    @BeforeEach
    void setUp() {
        savedTag = TagJpa.builder()
                .id(testTagId)
                .name(testName)
                .projectId(testProjectId)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        tagResponse = new TagResponse(
                testTagId,
                testName,
                testProjectId
        );

        createRequest = new CreateTagRequest(testName, testProjectId);
        updateRequest = new UpdateTagRequest("Updated Name");
    }

    @Test
    void getAllTags_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TagJpa> mockPage = new PageImpl<>(List.of(savedTag));
        Page<TagResponse> mockResponsePage = new PageImpl<>(List.of(tagResponse));

        when(tagRepository.findAll(pageable)).thenReturn(mockPage);
        when(tagMapper.toTagResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Mono<Page<TagResponse>> resultMono = tagService.getAllTags(pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().id().equals(testTagId))
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
        verify(tagMapper, never()).toTagResponse(any(TagJpa.class));
    }

    @Test
    void getTagsByProject_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TagJpa> mockPage = new PageImpl<>(List.of(savedTag));
        Page<TagResponse> mockResponsePage = new PageImpl<>(List.of(tagResponse));

        var projectJpa = com.example.colaba.project.entity.ProjectJpa.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(projectJpa));
        when(tagRepository.findByProjectId(testProjectId, pageable)).thenReturn(mockPage);
        when(tagMapper.toTagResponsePage(mockPage)).thenReturn(mockResponsePage);

        // When
        Mono<Page<TagResponse>> resultMono = tagService.getTagsByProject(testProjectId, pageable);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().id().equals(testTagId))
                .verifyComplete();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectId(testProjectId, pageable);
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
        verify(tagRepository, never()).findByProjectId(anyLong(), any(Pageable.class));
    }

    @Test
    void createTag_success() {
        // Given
        var projectJpa = com.example.colaba.project.entity.ProjectJpa.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(projectJpa));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagJpa.class))).thenReturn(savedTag);
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
        verify(tagRepository).save(argThat(tag ->
                testName.equals(tag.getName()) &&
                        testProjectId.equals(tag.getProjectId())));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void createTag_duplicateName_throwsException() {
        // Given
        var projectJpa = com.example.colaba.project.entity.ProjectJpa.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(projectJpa));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.of(savedTag));

        // When
        Mono<TagResponse> resultMono = tagService.createTag(createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof DuplicateTagException &&
                                throwable.getMessage().contains(testName) &&
                                throwable.getMessage().contains(String.valueOf(testProjectId)))
                .verify();

        verify(projectService).getProjectEntityById(testProjectId);
        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, testName);
        verify(tagRepository, never()).save(any(TagJpa.class));
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
        verify(tagRepository, never()).save(any(TagJpa.class));
    }

    @Test
    void updateTag_success_withChange() {
        // Given
        String newName = "Updated Tag";
        UpdateTagRequest changeRequest = new UpdateTagRequest(newName);
        TagJpa updatedTag = TagJpa.builder()
                .id(testTagId)
                .name(newName)
                .projectId(testProjectId)
                .build();
        TagResponse updatedResponse = new TagResponse(testTagId, newName, testProjectId);

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, newName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagJpa.class))).thenReturn(updatedTag);
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
        verify(tagRepository, never()).save(any(TagJpa.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void updateTag_duplicateName_throwsException() {
        // Given
        UpdateTagRequest duplicateRequest = new UpdateTagRequest("Duplicate Name");
        TagJpa duplicateTag = TagJpa.builder()
                .id(2L)
                .name("Duplicate Name")
                .projectId(testProjectId)
                .build();

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
        verify(tagRepository, never()).save(any(TagJpa.class));
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
        verify(tagRepository, never()).save(any(TagJpa.class));
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
        verify(tagRepository, never()).save(any(TagJpa.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void updateTag_emptyName_ignoresAndReturnsUnchanged() {
        // Given
        UpdateTagRequest emptyNameRequest = new UpdateTagRequest("");

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, emptyNameRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(TagJpa.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void updateTag_blankName_ignoresAndReturnsUnchanged() {
        // Given
        UpdateTagRequest blankNameRequest = new UpdateTagRequest("   ");

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, blankNameRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(response ->
                        response.id().equals(testTagId) &&
                                response.name().equals(testName))
                .verifyComplete();

        verify(tagRepository).findById(testTagId);
        verify(tagRepository, never()).findByProjectIdAndNameIgnoreCase(anyLong(), anyString());
        verify(tagRepository, never()).save(any(TagJpa.class));
        verify(tagMapper).toTagResponse(savedTag);
    }

    @Test
    void deleteTag_success() {
        // Given
        when(tagRepository.existsById(testTagId)).thenReturn(true);

        // When
        Mono<Void> resultMono = tagService.deleteTag(testTagId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(tagRepository).existsById(testTagId);
        verify(taskServiceClient).deleteTaskTagsByTagId(testTagId);
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
        verify(taskServiceClient, never()).deleteTaskTagsByTagId(anyLong());
        verify(tagRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteTag_callsTaskServiceClient() {
        // Given
        when(tagRepository.existsById(testTagId)).thenReturn(true);

        // When
        tagService.deleteTag(testTagId).block();

        // Then
        verify(taskServiceClient).deleteTaskTagsByTagId(testTagId);
    }

    @Test
    void updateTag_usesProjectIdFromTagForDuplicateCheck() {
        // Given
        String newName = "New Name";
        UpdateTagRequest changeRequest = new UpdateTagRequest(newName);

        TagJpa updatedTag = TagJpa.builder()
                .id(testTagId)
                .name(newName)
                .projectId(testProjectId)
                .build();
        TagResponse updatedResponse = new TagResponse(testTagId, newName, testProjectId);

        when(tagRepository.findById(testTagId)).thenReturn(Optional.of(savedTag));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, newName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagJpa.class))).thenReturn(updatedTag);
        when(tagMapper.toTagResponse(updatedTag)).thenReturn(updatedResponse);

        // When
        Mono<TagResponse> resultMono = tagService.updateTag(testTagId, changeRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextCount(1)
                .verifyComplete();

        verify(tagRepository).findByProjectIdAndNameIgnoreCase(testProjectId, newName);
    }

    @Test
    void createTag_savesWithCorrectProjectId() {
        // Given
        var projectJpa = com.example.colaba.project.entity.ProjectJpa.builder()
                .id(testProjectId)
                .name("Test Project")
                .build();

        when(projectService.getProjectEntityById(testProjectId)).thenReturn(Mono.just(projectJpa));
        when(tagRepository.findByProjectIdAndNameIgnoreCase(testProjectId, testName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(TagJpa.class))).thenReturn(savedTag);
        when(tagMapper.toTagResponse(savedTag)).thenReturn(tagResponse);

        // When
        Mono<TagResponse> resultMono = tagService.createTag(createRequest);

        // Then
        StepVerifier.create(resultMono)
                .expectNextCount(1)
                .verifyComplete();

        verify(tagRepository).save(argThat(tag ->
                testProjectId.equals(tag.getProjectId())));
    }
}