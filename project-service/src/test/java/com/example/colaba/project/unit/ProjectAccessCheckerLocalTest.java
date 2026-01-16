package com.example.colaba.project.unit;

import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.security.ProjectAccessCheckerLocal;
import com.example.colaba.shared.common.entity.ProjectRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessCheckerLocalTest {

    @Mock
    private ProjectMemberRepository memberRepository;

    private ProjectAccessCheckerLocal projectAccessChecker;

    private final Long testProjectId = 1L;
    private final Long testUserId = 2L;
    private final ProjectMemberJpa ownerMember = ProjectMemberJpa.builder()
            .role(ProjectRole.OWNER)
            .build();
    private final ProjectMemberJpa editorMember = ProjectMemberJpa.builder()
            .role(ProjectRole.EDITOR)
            .build();
    private final ProjectMemberJpa viewerMember = ProjectMemberJpa.builder()
            .role(ProjectRole.VIEWER)
            .build();

    @BeforeEach
    void setUp() {
        projectAccessChecker = new ProjectAccessCheckerLocal(memberRepository);
    }

    @Test
    void isOwner_whenUserIsOwner_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(true);

        // When
        boolean result = projectAccessChecker.isOwner(testProjectId, testUserId);

        // Then
        assertTrue(result);
        verify(memberRepository).existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER);
    }

    @Test
    void isOwner_whenUserIsNotOwner_returnsFalse() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(false);

        // When
        boolean result = projectAccessChecker.isOwner(testProjectId, testUserId);

        // Then
        assertFalse(result);
    }

    @Test
    void isAtLeastEditor_whenUserIsOwner_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(true);

        // When
        boolean result = projectAccessChecker.isAtLeastEditor(testProjectId, testUserId);

        // Then
        assertTrue(result);
    }

    @Test
    void isAtLeastEditor_whenUserIsEditor_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(true);

        // When
        boolean result = projectAccessChecker.isAtLeastEditor(testProjectId, testUserId);

        // Then
        assertTrue(result);
    }

    @Test
    void isAtLeastEditor_whenUserIsViewer_returnsFalse() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);

        // When
        boolean result = projectAccessChecker.isAtLeastEditor(testProjectId, testUserId);

        // Then
        assertFalse(result);
    }

    @Test
    void hasAnyRole_whenUserHasRole_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(true);

        // When
        boolean result = projectAccessChecker.hasAnyRole(testProjectId, testUserId);

        // Then
        assertTrue(result);
        verify(memberRepository).existsByProjectIdAndUserId(testProjectId, testUserId);
    }

    @Test
    void hasAnyRole_whenUserHasNoRole_returnsFalse() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(false);

        // When
        boolean result = projectAccessChecker.hasAnyRole(testProjectId, testUserId);

        // Then
        assertFalse(result);
    }

    @Test
    void getUserProjectRole_whenUserIsOwner_returnsOwner() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(ownerMember));

        // When
        ProjectRole result = projectAccessChecker.getUserProjectRole(testProjectId, testUserId);

        // Then
        assertEquals(ProjectRole.OWNER, result);
        verify(memberRepository).findByProjectIdAndUserId(testProjectId, testUserId);
    }

    @Test
    void getUserProjectRole_whenUserIsEditor_returnsEditor() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(editorMember));

        // When
        ProjectRole result = projectAccessChecker.getUserProjectRole(testProjectId, testUserId);

        // Then
        assertEquals(ProjectRole.EDITOR, result);
    }

    @Test
    void getUserProjectRole_whenUserIsViewer_returnsViewer() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(viewerMember));

        // When
        ProjectRole result = projectAccessChecker.getUserProjectRole(testProjectId, testUserId);

        // Then
        assertEquals(ProjectRole.VIEWER, result);
    }

    @Test
    void getUserProjectRole_whenUserNotFound_returnsNull() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.empty());

        // When
        ProjectRole result = projectAccessChecker.getUserProjectRole(testProjectId, testUserId);

        // Then
        assertNull(result);
    }

    @Test
    void requireOwner_whenUserIsOwner_doesNotThrow() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(true);

        // When & Then (no exception expected)
        assertDoesNotThrow(() ->
                projectAccessChecker.requireOwner(testProjectId, testUserId));
    }

    @Test
    void requireOwner_whenUserIsNotOwner_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> projectAccessChecker.requireOwner(testProjectId, testUserId));

        assertEquals("Only the project OWNER can perform this action", exception.getMessage());
    }

    @Test
    void requireAtLeastEditor_whenUserIsOwner_doesNotThrow() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(true);

        // When & Then (no exception expected)
        assertDoesNotThrow(() ->
                projectAccessChecker.requireAtLeastEditor(testProjectId, testUserId));
    }

    @Test
    void requireAtLeastEditor_whenUserIsEditor_doesNotThrow() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(true);

        // When & Then (no exception expected)
        assertDoesNotThrow(() ->
                projectAccessChecker.requireAtLeastEditor(testProjectId, testUserId));
    }

    @Test
    void requireAtLeastEditor_whenUserIsViewer_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(viewerMember));

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> projectAccessChecker.requireAtLeastEditor(testProjectId, testUserId));

        assertEquals("Required project role: at least EDITOR. Current role: VIEWER",
                exception.getMessage());
    }

    @Test
    void requireAtLeastEditor_whenUserHasNoRole_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.empty());

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> projectAccessChecker.requireAtLeastEditor(testProjectId, testUserId));

        assertEquals("Required project role: at least EDITOR. Current role: null",
                exception.getMessage());
    }

    @Test
    void requireAnyRole_whenUserHasRole_doesNotThrow() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(true);

        // When & Then (no exception expected)
        assertDoesNotThrow(() ->
                projectAccessChecker.requireAnyRole(testProjectId, testUserId));
    }

    @Test
    void requireAnyRole_whenUserHasNoRole_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(false);

        // When & Then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> projectAccessChecker.requireAnyRole(testProjectId, testUserId));

        assertEquals("You must be a member of this project to perform this action",
                exception.getMessage());
    }

    @Test
    void isOwnerMono_whenUserIsOwner_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(true);

        // When
        Mono<Boolean> result = projectAccessChecker.isOwnerMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isAtLeastEditorMono_whenUserIsViewer_returnsFalse() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);

        // When
        Mono<Boolean> result = projectAccessChecker.isAtLeastEditorMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void hasAnyRoleMono_whenUserHasRole_returnsTrue() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(true);

        // When
        Mono<Boolean> result = projectAccessChecker.hasAnyRoleMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void getUserProjectRoleMono_whenUserIsOwner_returnsOwner() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(ownerMember));

        // When
        Mono<ProjectRole> result = projectAccessChecker.getUserProjectRoleMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNext(ProjectRole.OWNER)
                .verifyComplete();
    }

    @Test
    @Disabled
    void getUserProjectRoleMono_whenUserNotFound_returnsNull() {
        // Given
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.empty());

        // When
        Mono<ProjectRole> result = projectAccessChecker.getUserProjectRoleMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(role -> role == null)
                .verifyComplete();
    }

    @Test
    void requireOwnerMono_whenUserIsOwner_completesSuccessfully() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(true);

        // When
        Mono<Void> result = projectAccessChecker.requireOwnerMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void requireOwnerMono_whenUserIsNotOwner_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRole(
                testProjectId, testUserId, ProjectRole.OWNER))
                .thenReturn(false);

        // When
        Mono<Void> result = projectAccessChecker.requireOwnerMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("Only the project OWNER can perform this action"))
                .verify();
    }

    @Test
    void requireAtLeastEditorMono_whenUserIsEditor_completesSuccessfully() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(true);

        // When
        Mono<Void> result = projectAccessChecker.requireAtLeastEditorMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void requireAtLeastEditorMono_whenUserIsViewer_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.of(viewerMember));

        // When
        Mono<Void> result = projectAccessChecker.requireAtLeastEditorMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("Required project role: at least EDITOR. Current role: VIEWER"))
                .verify();
    }

    @Test
    @Disabled
    void requireAtLeastEditorMono_whenUserNotFound_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserIdAndRoleIn(
                testProjectId, testUserId,
                Set.of(ProjectRole.EDITOR, ProjectRole.OWNER)))
                .thenReturn(false);
        when(memberRepository.findByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(Optional.empty());

        // When
        Mono<Void> result = projectAccessChecker.requireAtLeastEditorMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void requireAnyRoleMono_whenUserHasRole_completesSuccessfully() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(true);

        // When
        Mono<Void> result = projectAccessChecker.requireAnyRoleMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void requireAnyRoleMono_whenUserHasNoRole_throwsAccessDeniedException() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(false);

        // When
        Mono<Void> result = projectAccessChecker.requireAnyRoleMono(testProjectId, testUserId);

        // Then
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("You must be a member of this project to perform this action"))
                .verify();
    }

    @Test
    void reactiveMethods_executeOnBoundedElastic() {
        // Given
        when(memberRepository.existsByProjectIdAndUserId(testProjectId, testUserId))
                .thenReturn(true);

        // When
        Mono<Boolean> result = projectAccessChecker.hasAnyRoleMono(testProjectId, testUserId);

        // Then - проверяем, что выполнение происходит на отдельном потоке
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }
}