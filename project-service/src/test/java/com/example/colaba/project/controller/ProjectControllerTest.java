//package com.example.colaba.project.controller;
//
//import com.example.colaba.project.dto.project.CreateProjectRequest;
//import com.example.colaba.project.dto.project.ProjectScrollResponse;
//import com.example.colaba.project.dto.project.UpdateProjectRequest;
//import com.example.colaba.project.mapper.ProjectMapperImpl;
//import com.example.colaba.project.service.ProjectService;
//import com.example.colaba.project.service.ProjectServicePublic;
//import com.example.colaba.project.service.TagService;
//import com.example.colaba.project.service.TagServicePublic;
//import com.example.colaba.shared.common.dto.project.ProjectResponse;
//import com.example.colaba.shared.common.dto.tag.TagResponse;
//import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
//import com.example.colaba.shared.webflux.circuit.UserServiceClientWrapper;
//import com.example.colaba.shared.webflux.exception.ReactiveGlobalExceptionHandler;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Import;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Mono;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//
//@ComponentScan(basePackages = {
//        "com.example.colaba.project",
//        "com.example.colaba.shared.common",
//        "com.example.colaba.shared.webflux"
//})
//@TestPropertySource(properties = {
//        "internal.api-key'=supersecretbase64valuehereatleast32byteslong==",
//        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
//        "jwt.expiration=3600000",
//        "jwt.issuer=colaba",
/// /        "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
/// /        "spring.r2dbc.username=sa",
/// /        "spring.r2dbc.password=",
/// /        "spring.r2dbc.driver-class-name=io.r2dbc.h2.H2ConnectionFactory"
//})
//@SpringBootTest
//@AutoConfigureWebTestClient
//class ProjectControllerTest {
//
//    @Autowired
//    private WebTestClient webTestClient;
//
//    @MockitoBean
//    private ProjectServicePublic projectService;
//
//    @MockitoBean
//    private TagServicePublic tagService;
//
//    @MockitoBean
//    private UserServiceClientWrapper userServiceClient;
//
//    @MockitoBean
//    private TaskServiceClientWrapper taskServiceClient;
//
//    private static final Long ADMIN_ID = 1L;
//    private static final Long OWNER_ID = 2L;
//    private static final Long OTHER_ID = 3L;
//    private static final Long PROJECT_ID = 10L;
//
//    private final ProjectResponse sampleProject = ProjectResponse.builder()
//            .id(PROJECT_ID)
//            .name("Test Project")
//            .description("Description")
//            .ownerId(OWNER_ID)
//            .build();
//
//    @BeforeEach
//    void setUp() {
//        when(userServiceClient.isAdmin(ADMIN_ID)).thenReturn(true);
//        when(userServiceClient.isAdmin(OWNER_ID)).thenReturn(false);
//        when(userServiceClient.isAdmin(OTHER_ID)).thenReturn(false);
//
//        when(userServiceClient.userExists(anyLong())).thenReturn(true);
//
//        when(projectService.deleteProject(eq(PROJECT_ID), anyLong())).thenReturn(Mono.empty());
//    }
//
//    private WebTestClient authenticatedClient(Long userId) {
//        Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
//        return webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(auth));
//    }
//
//    @Test
//    void create_success() {
//        CreateProjectRequest request = new CreateProjectRequest("New Project", "Desc", OWNER_ID);
//        when(projectService.createProject(any(CreateProjectRequest.class), eq(OWNER_ID))).thenReturn(Mono.just(sampleProject));
//
//        authenticatedClient(OWNER_ID)
//                .post()
//                .uri("/api/projects")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isCreated()
//                .expectHeader().location("/api/projects/" + PROJECT_ID)
//                .expectBody(ProjectResponse.class).isEqualTo(sampleProject);
//    }
//
//    @Test
//    void create_forbidden_notSelfOwner() {
//        CreateProjectRequest request = new CreateProjectRequest("New Project", "Desc", OTHER_ID);
//
//        authenticatedClient(OWNER_ID)
//                .post()
//                .uri("/api/projects")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void update_asOwner_success() {
//        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "New Desc", null);
//        when(projectService.updateProject(eq(PROJECT_ID), any(UpdateProjectRequest.class), eq(OWNER_ID)))
//                .thenReturn(Mono.just(sampleProject));
//
//        authenticatedClient(OWNER_ID)
//                .put()
//                .uri("/api/projects/" + PROJECT_ID)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void update_asAdmin_success() {
//        UpdateProjectRequest request = new UpdateProjectRequest("Updated Name", "New Desc", null);
//        when(projectService.updateProject(eq(PROJECT_ID), any(UpdateProjectRequest.class), eq(OWNER_ID)))
//                .thenReturn(Mono.just(sampleProject));
//
//        authenticatedClient(ADMIN_ID)
//                .put()
//                .uri("/api/projects/" + PROJECT_ID)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(request)
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void update_asOther_forbidden() {
//        authenticatedClient(OTHER_ID)
//                .put()
//                .uri("/api/projects/" + PROJECT_ID)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(new UpdateProjectRequest("Name", "Desc", null))
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void changeOwner_asAdmin_success() {
//        when(projectService.changeProjectOwner(eq(PROJECT_ID), eq(OTHER_ID), eq(OWNER_ID)))
//                .thenReturn(Mono.just(sampleProject));
//
//        authenticatedClient(ADMIN_ID)
//                .patch()
//                .uri("/api/projects/" + PROJECT_ID + "/owner")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(Map.of("ownerId", OTHER_ID))
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void changeOwner_invalidRequest_badRequest() {
//        authenticatedClient(ADMIN_ID)
//                .patch()
//                .uri("/api/projects/" + PROJECT_ID + "/owner")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(Map.of())
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//
//    @Test
//    void getById_success() {
//        when(projectService.getProjectById(PROJECT_ID, OWNER_ID)).thenReturn(Mono.just(sampleProject));
//
//        authenticatedClient(OWNER_ID)
//                .get()
//                .uri("/api/projects/" + PROJECT_ID)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(ProjectResponse.class).isEqualTo(sampleProject);
//    }
//
//    @Test
//    void getAll_asAdmin_success() {
//        Page<ProjectResponse> page = new PageImpl<>(List.of(sampleProject), PageRequest.of(0, 20), 1);
//        when(projectService.getAllProjects(any(), ADMIN_ID)).thenReturn(Mono.just(page));
//
//        authenticatedClient(ADMIN_ID)
//                .get()
//                .uri("/api/projects?page=0&size=20")
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void getAll_asNonAdmin_success() {  // возвращает только свои проекты
//        Page<ProjectResponse> page = new PageImpl<>(List.of(sampleProject));
//        when(projectService.getAllProjects(any(), OWNER_ID)).thenReturn(Mono.just(page));
//
//        authenticatedClient(OWNER_ID)
//                .get()
//                .uri("/api/projects")
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void getByOwner_asAdmin_success() {
//        when(projectService.getProjectByOwnerId(OWNER_ID, ADMIN_ID)).thenReturn(Mono.just(List.of(sampleProject)));
//
//        authenticatedClient(ADMIN_ID)
//                .get()
//                .uri("/api/projects/owner/" + OWNER_ID)
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void getByOwner_asSelf_success() {
//        when(projectService.getProjectByOwnerId(OWNER_ID, OWNER_ID)).thenReturn(Mono.just(List.of(sampleProject)));
//
//        authenticatedClient(OWNER_ID)
//                .get()
//                .uri("/api/projects/owner/" + OWNER_ID)
//                .exchange()
//                .expectStatus().isOk();
//    }
//
//    @Test
//    void getByOwner_asOther_forbidden() {
//        authenticatedClient(OTHER_ID)
//                .get()
//                .uri("/api/projects/owner/" + OWNER_ID)
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void scroll_asAdmin_success() {
//        ProjectScrollResponse response = new ProjectScrollResponse(List.of(sampleProject), true, 10L);
//        when(projectService.scroll(0, 20, ADMIN_ID)).thenReturn(Mono.just(response));
//
//        authenticatedClient(ADMIN_ID)
//                .get()
//                .uri("/api/projects/scroll?page=0&size=20")
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody(ProjectScrollResponse.class).isEqualTo(response);
//    }
//
//    @Test
//    void delete_asOwner_success() {
//        authenticatedClient(OWNER_ID)
//                .delete()
//                .uri("/api/projects/" + PROJECT_ID)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    @Test
//    void delete_asAdmin_success() {
//        authenticatedClient(ADMIN_ID)
//                .delete()
//                .uri("/api/projects/" + PROJECT_ID)
//                .exchange()
//                .expectStatus().isNoContent();
//    }
//
//    @Test
//    void delete_asOther_forbidden() {
//        authenticatedClient(OTHER_ID)
//                .delete()
//                .uri("/api/projects/" + PROJECT_ID)
//                .exchange()
//                .expectStatus().isForbidden();
//    }
//
//    @Test
//    void create_invalidRequest_badRequest() {
//        CreateProjectRequest invalid = new CreateProjectRequest("", "", null); // пустые поля
//
//        authenticatedClient(OWNER_ID)
//                .post()
//                .uri("/api/projects")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(invalid)
//                .exchange()
//                .expectStatus().isBadRequest();
//    }
//}