package controller;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.service.ProjectMemberServicePublic;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.common.entity.UserRole;

import com.example.colaba.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ComponentScan(basePackages = {
        "com.example.colaba.project",
        "com.example.colaba.shared.common",
        "com.example.colaba.shared.webflux",
        "com.example.colaba.user"
})
@TestPropertySource(properties = {
        "internal.api-key=supersecretbase64valuehereatleast32byteslong==",
        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
        "jwt.expiration=3600000",
        "jwt.issuer=colaba",
        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.r2dbc.username=sa",
        "spring.r2dbc.password=",
        "spring.r2dbc.driver-class-name=io.r2dbc.h2.H2ConnectionFactory"
})
@SpringBootTest
class ProjectControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProjectMemberServicePublic projectMemberService;

    @MockitoBean
    private UserRepository userRepository;

    private static final Long ADMIN_ID    = 1L;
    private static final Long OWNER_ID    = 2L;
    private static final Long MEMBER_ID   = 3L;
    private static final Long OTHER_ID    = 4L;
    private static final Long PROJECT_ID  = 100L;

    private final ProjectMemberResponse sampleMember = new ProjectMemberResponse(
            PROJECT_ID,
            MEMBER_ID,
            "MEMBER"
    );

    @BeforeEach
    void setUp() {
        when(userRepository.existsByIdAndRole(ADMIN_ID, UserRole.ADMIN)).thenReturn(Mono.just(true));
        when(userRepository.existsByIdAndRole(OWNER_ID,  UserRole.ADMIN)).thenReturn(Mono.just(false));
        when(userRepository.existsByIdAndRole(MEMBER_ID, UserRole.ADMIN)).thenReturn(Mono.just(false));
        when(userRepository.existsByIdAndRole(OTHER_ID,  UserRole.ADMIN)).thenReturn(Mono.just(false));
    }

    private WebTestClient authenticatedClient(Long userId) {
        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        return webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth));
    }


    @Test
    void getMembersByProject_success_asMember() {
        Page<ProjectMemberResponse> page = new PageImpl<>(
                List.of(sampleMember),
                PageRequest.of(0, 20),
                1
        );

        when(projectMemberService.getMembersByProject(eq(PROJECT_ID), any(), eq(OWNER_ID)))
                .thenReturn(Mono.just(page));

        authenticatedClient(OWNER_ID)
                .get()
                .uri("/api/projects/" + PROJECT_ID + "/members?page=0&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].userId").isEqualTo(MEMBER_ID)
                .jsonPath("$.content[0].role").isEqualTo(ProjectRole.OWNER);
    }

    @Test
    void addMember_success_asOwner() {
        CreateProjectMemberRequest req = new CreateProjectMemberRequest(MEMBER_ID, ProjectRole.EDITOR);
        when(projectMemberService.createMembership(
                eq(PROJECT_ID),
                any(CreateProjectMemberRequest.class),
                eq(OWNER_ID)
        )).thenReturn(Mono.just(sampleMember));

        authenticatedClient(OWNER_ID)
                .post()
                .uri("/api/projects/" + PROJECT_ID + "/members")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectMemberResponse.class)
                .isEqualTo(sampleMember);
    }

    @Test
    void addMember_asRegularMember_forbidden() {
        CreateProjectMemberRequest req = new CreateProjectMemberRequest(OTHER_ID, ProjectRole.OWNER);

        authenticatedClient(MEMBER_ID)
                .post()
                .uri("/api/projects/" + PROJECT_ID + "/members")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void updateMemberRole_success_asOwner() {
        UpdateProjectMemberRequest req = new UpdateProjectMemberRequest(ProjectRole.EDITOR);

        // Для обновлённой версии просто создаём новый record
        ProjectMemberResponse updated = new ProjectMemberResponse(
                PROJECT_ID,
                MEMBER_ID,
                "EDITOR"
        );

        when(projectMemberService.updateMembership(eq(PROJECT_ID), eq(MEMBER_ID), any(UpdateProjectMemberRequest.class), eq(OWNER_ID)))
                .thenReturn(Mono.just(updated));

        authenticatedClient(OWNER_ID)
                .put()
                .uri("/api/projects/" + PROJECT_ID + "/members/" + MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.role").isEqualTo("EDITOR");
    }

    @Test
    void updateMemberRole_asOtherUser_forbidden() {
        UpdateProjectMemberRequest req = new UpdateProjectMemberRequest(ProjectRole.EDITOR);

        authenticatedClient(OTHER_ID)
                .put()
                .uri("/api/projects/" + PROJECT_ID + "/members/" + MEMBER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void removeMember_success_asOwner() {
        when(projectMemberService.deleteMembership(eq(PROJECT_ID), eq(MEMBER_ID), eq(OWNER_ID)))
                .thenReturn(Mono.empty());

        authenticatedClient(OWNER_ID)
                .delete()
                .uri("/api/projects/" + PROJECT_ID + "/members/" + MEMBER_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void removeMember_asNonPermitted_forbidden() {
        authenticatedClient(MEMBER_ID)
                .delete()
                .uri("/api/projects/" + PROJECT_ID + "/members/" + OTHER_ID)
                .exchange()
                .expectStatus().isForbidden();
    }
}