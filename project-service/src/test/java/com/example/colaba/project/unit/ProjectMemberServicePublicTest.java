package com.example.colaba.project.unit;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.project.service.ProjectMemberService;
import com.example.colaba.project.service.ProjectMemberServicePublic;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.webflux.client.UserServiceClient;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectMemberServicePublicTest {

    @Mock
    private ProjectMemberService projectMemberService;

    @Mock
    private ProjectAccessCheckerLocal accessChecker;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ProjectMemberServicePublic projectMemberServicePublic;

    private final Long testProjectId = 1L;
    private final Long testUserId = 2L;
    private final Long testCurrentUserId = 3L;
    private final Long testOtherUserId = 4L;
    private final ProjectMemberResponse testMemberResponse = new ProjectMemberResponse(
            testUserId,
            testProjectId,
            "EDITOR",
            OffsetDateTime.now()
    );
    private Page<ProjectMemberResponse> testPage;
    private CreateProjectMemberRequest createRequest;
    private UpdateProjectMemberRequest updateRequest;

    @BeforeEach
    void setUp() {
        List<ProjectMemberResponse> members = List.of(testMemberResponse);
        Pageable pageable = PageRequest.of(0, 10);
        testPage = new PageImpl<>(members, pageable, members.size());

        createRequest = new CreateProjectMemberRequest(testUserId, ProjectRole.EDITOR);
        updateRequest = new UpdateProjectMemberRequest(ProjectRole.VIEWER);
    }

    @Test
    void getMembersByProject_userIsAdmin_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.getMembersByProject(testProjectId, pageable))
                .thenReturn(Mono.just(testPage));

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberServicePublic
                .getMembersByProject(testProjectId, pageable, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testPage)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(projectMemberService).getMembersByProject(testProjectId, pageable);
        verify(accessChecker, never()).requireAtLeastEditorMono(anyLong(), anyLong());
    }

    @Test
    void getMembersByProject_userIsNotAdminButEditor_success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireAtLeastEditorMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.empty());
        when(projectMemberService.getMembersByProject(testProjectId, pageable))
                .thenReturn(Mono.just(testPage));

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberServicePublic
                .getMembersByProject(testProjectId, pageable, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testPage)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireAtLeastEditorMono(testProjectId, testCurrentUserId);
        verify(projectMemberService).getMembersByProject(testProjectId, pageable);
    }

    @Test
    void getMembersByProject_userIsNotAdminAndNotEditor_accessDenied() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException accessDeniedException = new RuntimeException("Access denied");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireAtLeastEditorMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.error(accessDeniedException));

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberServicePublic
                .getMembersByProject(testProjectId, pageable, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == accessDeniedException)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireAtLeastEditorMono(testProjectId, testCurrentUserId);
        verify(projectMemberService, never()).getMembersByProject(anyLong(), any());
    }

    @Test
    void getMembersByProject_adminCheckError_propagatesError() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException serviceError = new RuntimeException("Service error");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.error(serviceError));

        // When
        Mono<Page<ProjectMemberResponse>> resultMono = projectMemberServicePublic
                .getMembersByProject(testProjectId, pageable, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == serviceError)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker, never()).requireAtLeastEditorMono(anyLong(), anyLong());
        verify(projectMemberService, never()).getMembersByProject(anyLong(), any());
    }

    @Test
    void createMembership_userIsAdmin_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.createMembership(testProjectId, createRequest))
                .thenReturn(Mono.just(testMemberResponse));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .createMembership(testProjectId, createRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testMemberResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(projectMemberService).createMembership(testProjectId, createRequest);
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
    }

    @Test
    void createMembership_userIsNotAdminButOwner_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.empty());
        when(projectMemberService.createMembership(testProjectId, createRequest))
                .thenReturn(Mono.just(testMemberResponse));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .createMembership(testProjectId, createRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testMemberResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService).createMembership(testProjectId, createRequest);
    }

    @Test
    void createMembership_userIsNotAdminAndNotOwner_accessDenied() {
        // Given
        RuntimeException accessDeniedException = new RuntimeException("Access denied");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.error(accessDeniedException));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .createMembership(testProjectId, createRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == accessDeniedException)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService, never()).createMembership(anyLong(), any());
    }

    @Test
    void createMembership_adminCheckError_propagatesError() {
        // Given
        RuntimeException serviceError = new RuntimeException("Service error");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.error(serviceError));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .createMembership(testProjectId, createRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == serviceError)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
        verify(projectMemberService, never()).createMembership(anyLong(), any());
    }

    @Test
    void createMembership_membershipServiceError_propagatesError() {
        // Given
        RuntimeException serviceError = new RuntimeException("Membership service error");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.createMembership(testProjectId, createRequest))
                .thenReturn(Mono.error(serviceError));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .createMembership(testProjectId, createRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == serviceError)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(projectMemberService).createMembership(testProjectId, createRequest);
    }

    @Test
    void updateMembership_userIsAdmin_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.updateMembership(testProjectId, testUserId, updateRequest))
                .thenReturn(Mono.just(testMemberResponse));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .updateMembership(testProjectId, testUserId, updateRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testMemberResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(projectMemberService).updateMembership(testProjectId, testUserId, updateRequest);
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
    }

    @Test
    void updateMembership_userIsNotAdminButOwner_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.empty());
        when(projectMemberService.updateMembership(testProjectId, testUserId, updateRequest))
                .thenReturn(Mono.just(testMemberResponse));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .updateMembership(testProjectId, testUserId, updateRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectNext(testMemberResponse)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService).updateMembership(testProjectId, testUserId, updateRequest);
    }

    @Test
    void updateMembership_userIsNotAdminAndNotOwner_accessDenied() {
        // Given
        RuntimeException accessDeniedException = new RuntimeException("Access denied");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.error(accessDeniedException));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .updateMembership(testProjectId, testUserId, updateRequest, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == accessDeniedException)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService, never()).updateMembership(anyLong(), anyLong(), any());
    }

    @Test
    void updateMembership_updatingSelfRole_asEditorNotAdmin_notAllowed() {
        // Given
        Long selfUserId = testCurrentUserId;
        RuntimeException accessDeniedException = new RuntimeException("Access denied");

        when(userServiceClient.isAdmin(selfUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, selfUserId))
                .thenReturn(Mono.error(accessDeniedException));

        // When
        Mono<ProjectMemberResponse> resultMono = projectMemberServicePublic
                .updateMembership(testProjectId, selfUserId, updateRequest, selfUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == accessDeniedException)
                .verify();

        verify(userServiceClient).isAdmin(selfUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, selfUserId);
        verify(projectMemberService, never()).updateMembership(anyLong(), anyLong(), any());
    }

    @Test
    void deleteMembership_userIsAdmin_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.deleteMembership(testProjectId, testUserId))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectMemberServicePublic
                .deleteMembership(testProjectId, testUserId, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(projectMemberService).deleteMembership(testProjectId, testUserId);
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
    }

    @Test
    void deleteMembership_userIsNotAdminButOwner_success() {
        // Given
        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.empty());
        when(projectMemberService.deleteMembership(testProjectId, testUserId))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectMemberServicePublic
                .deleteMembership(testProjectId, testUserId, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService).deleteMembership(testProjectId, testUserId);
    }

    @Test
    void deleteMembership_userIsNotAdminAndNotOwner_accessDenied() {
        // Given
        RuntimeException accessDeniedException = new RuntimeException("Access denied");

        when(userServiceClient.isAdmin(testCurrentUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, testCurrentUserId))
                .thenReturn(Mono.error(accessDeniedException));

        // When
        Mono<Void> resultMono = projectMemberServicePublic
                .deleteMembership(testProjectId, testUserId, testCurrentUserId);

        // Then
        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable == accessDeniedException)
                .verify();

        verify(userServiceClient).isAdmin(testCurrentUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, testCurrentUserId);
        verify(projectMemberService, never()).deleteMembership(anyLong(), anyLong());
    }

    @Test
    void deleteMembership_deletingSelfMembership_asOwner_success() {
        // Given
        Long selfUserId = testCurrentUserId;

        when(userServiceClient.isAdmin(selfUserId)).thenReturn(Mono.just(false));
        when(accessChecker.requireOwnerMono(testProjectId, selfUserId))
                .thenReturn(Mono.empty());
        when(projectMemberService.deleteMembership(testProjectId, selfUserId))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectMemberServicePublic
                .deleteMembership(testProjectId, selfUserId, selfUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userServiceClient).isAdmin(selfUserId);
        verify(accessChecker).requireOwnerMono(testProjectId, selfUserId);
        verify(projectMemberService).deleteMembership(testProjectId, selfUserId);
    }

    @Test
    void deleteMembership_deletingSelfMembership_asAdmin_success() {
        // Given
        Long selfUserId = testCurrentUserId;

        when(userServiceClient.isAdmin(selfUserId)).thenReturn(Mono.just(true));
        when(projectMemberService.deleteMembership(testProjectId, selfUserId))
                .thenReturn(Mono.empty());

        // When
        Mono<Void> resultMono = projectMemberServicePublic
                .deleteMembership(testProjectId, selfUserId, selfUserId);

        // Then
        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(userServiceClient).isAdmin(selfUserId);
        verify(projectMemberService).deleteMembership(testProjectId, selfUserId);
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
    }

    @Test
    void allMethods_adminCheckReturnsFalseThenAccessCheckerIsUsed() {
        // Test that for all methods, when isAdmin returns false, the access checker is consulted
        when(userServiceClient.isAdmin(anyLong())).thenReturn(Mono.just(false));
        when(accessChecker.requireAtLeastEditorMono(anyLong(), anyLong())).thenReturn(Mono.empty());
        when(accessChecker.requireOwnerMono(anyLong(), anyLong())).thenReturn(Mono.empty());
        when(projectMemberService.getMembersByProject(anyLong(), any())).thenReturn(Mono.just(testPage));
        when(projectMemberService.createMembership(anyLong(), any())).thenReturn(Mono.just(testMemberResponse));
        when(projectMemberService.updateMembership(anyLong(), anyLong(), any())).thenReturn(Mono.just(testMemberResponse));
        when(projectMemberService.deleteMembership(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Test all methods
        StepVerifier.create(projectMemberServicePublic
                        .getMembersByProject(testProjectId, PageRequest.of(0, 10), testCurrentUserId))
                .expectNext(testPage)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .createMembership(testProjectId, createRequest, testCurrentUserId))
                .expectNext(testMemberResponse)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .updateMembership(testProjectId, testUserId, updateRequest, testCurrentUserId))
                .expectNext(testMemberResponse)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .deleteMembership(testProjectId, testUserId, testCurrentUserId))
                .verifyComplete();

        verify(userServiceClient, times(4)).isAdmin(testCurrentUserId);
        verify(accessChecker).requireAtLeastEditorMono(eq(testProjectId), eq(testCurrentUserId));
        verify(accessChecker, times(3)).requireOwnerMono(eq(testProjectId), eq(testCurrentUserId));
    }

    @Test
    void allMethods_adminCheckReturnsTrueThenAccessCheckerIsNotUsed() {
        // Test that for all methods, when isAdmin returns true, the access checker is NOT consulted
        when(userServiceClient.isAdmin(anyLong())).thenReturn(Mono.just(true));
        when(projectMemberService.getMembersByProject(anyLong(), any())).thenReturn(Mono.just(testPage));
        when(projectMemberService.createMembership(anyLong(), any())).thenReturn(Mono.just(testMemberResponse));
        when(projectMemberService.updateMembership(anyLong(), anyLong(), any())).thenReturn(Mono.just(testMemberResponse));
        when(projectMemberService.deleteMembership(anyLong(), anyLong())).thenReturn(Mono.empty());

        // Test all methods
        StepVerifier.create(projectMemberServicePublic
                        .getMembersByProject(testProjectId, PageRequest.of(0, 10), testCurrentUserId))
                .expectNext(testPage)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .createMembership(testProjectId, createRequest, testCurrentUserId))
                .expectNext(testMemberResponse)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .updateMembership(testProjectId, testUserId, updateRequest, testCurrentUserId))
                .expectNext(testMemberResponse)
                .verifyComplete();

        StepVerifier.create(projectMemberServicePublic
                        .deleteMembership(testProjectId, testUserId, testCurrentUserId))
                .verifyComplete();

        verify(userServiceClient, times(4)).isAdmin(testCurrentUserId);
        verify(accessChecker, never()).requireAtLeastEditorMono(anyLong(), anyLong());
        verify(accessChecker, never()).requireOwnerMono(anyLong(), anyLong());
    }
}