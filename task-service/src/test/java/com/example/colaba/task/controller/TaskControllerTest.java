package com.example.colaba.task.controller;

import com.example.colaba.shared.common.security.JwtService;
import com.example.colaba.task.dto.task.CreateTaskRequest;
import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.dto.task.UpdateTaskRequest;
import com.example.colaba.task.service.TaskServicePublic;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "internal.api-key=supersecretbase64valuehereatleast32byteslong==",
        "jwt.secret=supersecretbase64valuehereatleast32byteslong==",
        "jwt.expiration=3600000",
        "jwt.issuer=colaba",
        "spring.datasource.url=jdbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.liquibase.enabled=false"
})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskServicePublic taskService;

    @MockitoBean
    private JwtService jwtService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        adminToken = "admin-token";
        userToken = "user-token";

        when(jwtService.validateToken("admin-token"))
                .thenReturn(new DefaultClaims(Map.of("sub", "1", "role", "ADMIN")));
        when(jwtService.validateToken("user-token"))
                .thenReturn(new DefaultClaims(Map.of("sub", "2", "role", "USER")));
    }


    // ----------- GET ALL TASKS -----------
    @Test
    void getAllTasks_success_asAdmin() throws Exception {
        Page<TaskResponse> page = new PageImpl<>(List.of(
                new TaskResponse(100L, "Title", "Desc", "TODO", "HIGH", 10L, 2L, 1L, null)
        ));
        when(taskService.getAllTasks(any(Pageable.class), eq(1L))).thenReturn(page);

        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L))
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void getAllTasks_forbidden_forNonAdmin() throws Exception {
        when(taskService.getAllTasks(any(Pageable.class), eq(2L)))
                .thenThrow(new AccessDeniedException("Required user role: ADMIN"));

        mockMvc.perform(get("/api/tasks")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ----------- GET TASK BY ID -----------
    @Test
    void getTaskById_success() throws Exception {
        TaskResponse response = new TaskResponse(100L, "Title", "Desc", "TODO", "HIGH", 10L, 2L, 1L, null);
        when(taskService.getTaskById(eq(100L), eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/tasks/{id}", 100L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    // ----------- GET TASKS BY PROJECT -----------
    @Test
    void getTasksByProject_success() throws Exception {
        Page<TaskResponse> page = new PageImpl<>(List.of(
                new TaskResponse(101L, "Task1", "Desc1", "TODO", "MEDIUM", 10L, 2L, 1L, null)
        ));
        when(taskService.getTasksByProject(eq(10L), any(Pageable.class), eq(1L))).thenReturn(page);

        mockMvc.perform(get("/api/tasks/project/{projectId}", 10L)
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(101L));
    }

    // ----------- GET TASKS BY ASSIGNEE -----------
    @Test
    void getTasksByAssignee_success_forSelf() throws Exception {
        Page<TaskResponse> page = new PageImpl<>(List.of(
                new TaskResponse(100L, "T1", "D1", "TODO", "HIGH", 10L, 2L, 1L, null)
        ));
        when(taskService.getTasksByAssignee(eq(2L), any(Pageable.class), eq(2L))).thenReturn(page);

        mockMvc.perform(get("/api/tasks/assignee/{userId}", 2L)
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(100L));
    }

    @Test
    void getTasksByAssignee_forbidden_forOtherUser() throws Exception {
        when(taskService.getTasksByAssignee(eq(3L), any(Pageable.class), eq(2L)))
                .thenThrow(new AccessDeniedException("You can only view your own assigned tasks"));

        mockMvc.perform(get("/api/tasks/assignee/{userId}", 3L)
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ----------- CREATE TASK -----------
    @Test
    void createTask_success_asAdmin() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Title", "Desc", null, null, 2L, 1L, null);
        TaskResponse response = new TaskResponse(100L, "Title", "Desc", "TODO", "MEDIUM", 10L, 2L, 1L, null);

        when(taskService.createTask(any(CreateTaskRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    void createTask_forbidden_forNonAdminWithoutAccess() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Title", "Desc", null, null, 2L, 2L, null);

        when(taskService.createTask(any(CreateTaskRequest.class), eq(2L)))
                .thenThrow(new AccessDeniedException("Required user role: ADMIN"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ----------- UPDATE TASK -----------
    @Test
    void updateTask_success() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest("Updated", "Desc", null, null, null, null);
        TaskResponse response = new TaskResponse(100L, "Updated", "Desc", "TODO", null, 10L, 2L, 1L, null);

        when(taskService.updateTask(eq(100L), any(UpdateTaskRequest.class), eq(1L))).thenReturn(response);

        mockMvc.perform(put("/api/tasks/{id}", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    // ----------- DELETE TASK -----------
    @Test
    void deleteTask_success_asAdmin() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 100L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_forbidden_forNonAdminWithoutAccess() throws Exception {
        doThrow(new AccessDeniedException("Required user role: ADMIN"))
                .when(taskService).deleteTask(eq(100L), eq(2L));

        mockMvc.perform(delete("/api/tasks/{id}", 100L)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}

