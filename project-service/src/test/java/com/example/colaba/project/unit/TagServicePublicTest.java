package com.example.colaba.project.unit;

import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.project.service.TagService;
import com.example.colaba.project.service.TagServicePublic;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
import com.example.colaba.shared.webflux.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServicePublicTest {

    @Mock
    private TagService tagService;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ProjectAccessCheckerLocal projectAccessCheckerLocal;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TagServicePublic tagServicePublic;

    private TagJpa testTag;
    private TagResponse testTagResponse;
    private final Long testId = 1L;
    private final Long testProjectId = 1L;
    private final Long adminUserId = 1L;
    private final Long regularUserId = 2L;
    private final Long editorUserId = 3L;
    private final String testTagName = "Test Tag";

    @BeforeEach
    void setUp() {
        testTag = TagJpa.builder()
                .id(testId)
                .name(testTagName)
                .projectId(testProjectId)
                .createdAt(OffsetDateTime.now())
                .build();

        testTagResponse = new TagResponse(
                testId,
                testTagName,
                testProjectId
        );
    }

    // ========== getAllTags Tests ==========

    @Test
    void getAllTags_adminUser_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TagResponse> mockPage = new PageImpl<>(List.of(testTagResponse));

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.getAllTags(pageable)).thenReturn(Mono.just(mockPage));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getAllTags(pageable, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(mockPage)
                .verifyComplete();

        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).getAllTags(pageable);
    }

    @Test
    void getAllTags_regularUser_throwsAccessDenied() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getAllTags(pageable, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains("Only ADMIN can view all tags"))
                .verify();

        verify(userServiceClient).isAdmin(regularUserId);
        verify(tagService, never()).getAllTags(any(Pageable.class));
    }

    // ========== getTagById Tests ==========

    @Test
    void getTagById_adminUser_success() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagMapper.toTagResponse(testTag)).thenReturn(testTagResponse);

        // When
        Mono<TagResponse> resultMono = tagServicePublic.getTagById(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(projectAccessCheckerLocal, never()).requireAnyRoleMono(anyLong(), anyLong());
        verify(tagMapper).toTagResponse(testTag);
    }

    @Test
    void getTagById_regularUserWithProjectAccess_success() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testProjectId, regularUserId))
                .thenReturn(Mono.empty());
        when(tagMapper.toTagResponse(testTag)).thenReturn(testTagResponse);

        // When
        Mono<TagResponse> resultMono = tagServicePublic.getTagById(testId, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testProjectId, regularUserId);
        verify(tagMapper).toTagResponse(testTag);
    }

    @Test
    void getTagById_regularUserWithoutProjectAccess_throwsAccessDenied() {
        // Given
        String errorMessage = "Access denied";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testProjectId, regularUserId))
                .thenReturn(Mono.error(exception));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.getTagById(testId, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testProjectId, regularUserId);
    }

    @Test
    void getTagById_tagNotFound_throwsException() {
        // Given
        when(tagService.getTagEntityById(testId))
                .thenReturn(Mono.error(new TagNotFoundException(testId)));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.getTagById(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(TagNotFoundException.class)
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient, never()).isAdmin(anyLong());
    }

    // ========== getTagsByProject Tests ==========

    @Test
    void getTagsByProject_adminUser_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TagResponse> mockPage = new PageImpl<>(List.of(testTagResponse));

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.getTagsByProject(testProjectId, pageable)).thenReturn(Mono.just(mockPage));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getTagsByProject(testProjectId, pageable, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(mockPage)
                .verifyComplete();

        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).getTagsByProject(testProjectId, pageable);
        verify(projectAccessCheckerLocal, never()).requireAnyRoleMono(anyLong(), anyLong());
    }

    @Test
    void getTagsByProject_regularUserWithAccess_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<TagResponse> mockPage = new PageImpl<>(List.of(testTagResponse));

        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testProjectId, regularUserId))
                .thenReturn(Mono.empty());
        when(tagService.getTagsByProject(testProjectId, pageable)).thenReturn(Mono.just(mockPage));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getTagsByProject(testProjectId, pageable, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(mockPage)
                .verifyComplete();

        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testProjectId, regularUserId);
        verify(tagService).getTagsByProject(testProjectId, pageable);
    }

    @Test
    void getTagsByProject_regularUserWithoutAccess_throwsAccessDenied() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Access denied";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAnyRoleMono(testProjectId, regularUserId))
                .thenReturn(Mono.error(exception));
        // Stub для избежания проблем
        when(tagService.getTagsByProject(testProjectId, pageable)).thenReturn(Mono.just(new PageImpl<>(List.of())));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getTagsByProject(testProjectId, pageable, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAnyRoleMono(testProjectId, regularUserId);
    }

    @Test
    void getTagsByProject_projectNotFound_throwsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.getTagsByProject(testProjectId, pageable))
                .thenReturn(Mono.error(new ProjectNotFoundException(testProjectId)));

        // When
        Mono<Page<TagResponse>> resultMono = tagServicePublic.getTagsByProject(testProjectId, pageable, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(ProjectNotFoundException.class)
                .verify();

        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).getTagsByProject(testProjectId, pageable);
    }

    // ========== createTag Tests ==========

    @Test
    void createTag_adminUser_success() {
        // Given
        CreateTagRequest request = new CreateTagRequest(testTagName, testProjectId);

        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.createTag(request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.createTag(request, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).createTag(request);
        verify(projectAccessCheckerLocal, never()).requireAtLeastEditorMono(anyLong(), anyLong());
    }

    @Test
    void createTag_editorUserWithAccess_success() {
        // Given
        CreateTagRequest request = new CreateTagRequest(testTagName, testProjectId);

        when(userServiceClient.isAdmin(editorUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, editorUserId))
                .thenReturn(Mono.empty());
        when(tagService.createTag(request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.createTag(request, editorUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(editorUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, editorUserId);
        verify(tagService).createTag(request);
    }

    @Test
    void createTag_regularUserWithoutEditorAccess_throwsAccessDenied() {
        // Given
        CreateTagRequest request = new CreateTagRequest(testTagName, testProjectId);
        String errorMessage = "Editor access required";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, regularUserId))
                .thenReturn(Mono.error(exception));
        // Stub
        when(tagService.createTag(request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.createTag(request, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, regularUserId);
    }

    // ========== updateTag Tests ==========

    @Test
    void updateTag_adminUser_success() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated Tag");

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.updateTag(testId, request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).updateTag(testId, request);
        verify(projectAccessCheckerLocal, never()).requireAtLeastEditorMono(anyLong(), anyLong());
    }

    @Test
    void updateTag_editorUserWithAccess_success() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated Tag");

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(editorUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, editorUserId))
                .thenReturn(Mono.empty());
        when(tagService.updateTag(testId, request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, editorUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(editorUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, editorUserId);
        verify(tagService).updateTag(testId, request);
    }

    @Test
    void updateTag_regularUserWithoutEditorAccess_throwsAccessDenied() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated Tag");
        String errorMessage = "Editor access required";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, regularUserId))
                .thenReturn(Mono.error(exception));
        // Stub
        when(tagService.updateTag(testId, request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, regularUserId);
    }

    @Test
    void updateTag_tagNotFound_throwsException() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated Tag");

        when(tagService.getTagEntityById(testId))
                .thenReturn(Mono.error(new TagNotFoundException(testId)));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(TagNotFoundException.class)
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient, never()).isAdmin(anyLong());
    }

    @Test
    void updateTag_updateThrowsException_propagatesException() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated Tag");
        String errorMessage = "Update failed";
        RuntimeException exception = new RuntimeException(errorMessage);

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.updateTag(testId, request))
                .thenReturn(Mono.error(exception));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).updateTag(testId, request);
    }

    // ========== deleteTag Tests ==========

    @Test
    void deleteTag_adminUser_success() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.deleteTag(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).deleteTag(testId);
        verify(projectAccessCheckerLocal, never()).requireAtLeastEditorMono(anyLong(), anyLong());
    }

    @Test
    void deleteTag_editorUserWithAccess_success() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(editorUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, editorUserId))
                .thenReturn(Mono.empty());
        when(tagService.deleteTag(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, editorUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(editorUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, editorUserId);
        verify(tagService).deleteTag(testId);
    }

    @Test
    void deleteTag_regularUserWithoutEditorAccess_throwsAccessDenied() {
        // Given
        String errorMessage = "Editor access required";
        AccessDeniedException exception = new AccessDeniedException(errorMessage);

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(regularUserId)).thenReturn(Mono.just(false));
        when(projectAccessCheckerLocal.requireAtLeastEditorMono(testProjectId, regularUserId))
                .thenReturn(Mono.error(exception));
        // Stub
        when(tagService.deleteTag(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, regularUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(regularUserId);
        verify(projectAccessCheckerLocal).requireAtLeastEditorMono(testProjectId, regularUserId);
    }

    @Test
    void deleteTag_tagNotFound_throwsException() {
        // Given
        when(tagService.getTagEntityById(testId))
                .thenReturn(Mono.error(new TagNotFoundException(testId)));

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(TagNotFoundException.class)
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient, never()).isAdmin(anyLong());
    }

    @Test
    void deleteTag_deleteThrowsException_propagatesException() {
        // Given
        String errorMessage = "Delete failed";
        RuntimeException exception = new RuntimeException(errorMessage);

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.deleteTag(testId))
                .thenReturn(Mono.error(exception));

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains(errorMessage))
                .verify();

        verify(tagService).getTagEntityById(testId);
        verify(userServiceClient).isAdmin(adminUserId);
        verify(tagService).deleteTag(testId);
    }

    // ========== Edge Cases Tests ==========

    @Test
    void updateTag_nullRequest_throwsNullPointer() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, null, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectError(NullPointerException.class)
                .verify();
    }

    // ========== Verification of Method Calls Tests ==========

    @Test
    void updateTag_verifiesCorrectMethodSequence() {
        // Given
        UpdateTagRequest request = new UpdateTagRequest("Updated");

        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.updateTag(testId, request)).thenReturn(Mono.just(testTagResponse));

        // When
        Mono<TagResponse> resultMono = tagServicePublic.updateTag(testId, request, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testTagResponse)
                .verifyComplete();

        InOrder inOrder = inOrder(tagService, userServiceClient);
        inOrder.verify(tagService).getTagEntityById(testId);
        inOrder.verify(userServiceClient).isAdmin(adminUserId);
        inOrder.verify(tagService).updateTag(testId, request);
    }

    @Test
    void deleteTag_verifiesCorrectMethodSequence() {
        // Given
        when(tagService.getTagEntityById(testId)).thenReturn(Mono.just(testTag));
        when(userServiceClient.isAdmin(adminUserId)).thenReturn(Mono.just(true));
        when(tagService.deleteTag(testId)).thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = tagServicePublic.deleteTag(testId, adminUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        InOrder inOrder = inOrder(tagService, userServiceClient);
        inOrder.verify(tagService).getTagEntityById(testId);
        inOrder.verify(userServiceClient).isAdmin(adminUserId);
        inOrder.verify(tagService).deleteTag(testId);
    }
}