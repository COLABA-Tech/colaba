package com.example.colaba.project.unit;

import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.project.mapper.ProjectMemberMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMemberMapperTest {

    private ProjectMemberMapper projectMemberMapper;

    @BeforeEach
    void setUp() {
        projectMemberMapper = Mappers.getMapper(ProjectMemberMapper.class);
    }

    @Test
    void toProjectMemberResponse_shouldReturnNullWhenInputIsNull() {
        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(null);
        assertNull(response);
    }

    @Test
    void toProjectMemberResponse_shouldMapMemberWithNullRole() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();
        memberJpa.setProjectId(1L);
        memberJpa.setUserId(100L);
        // role по умолчанию null, но можно явно установить
        // memberJpa.setRole(null);

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
        assertEquals(1L, response.projectId()); // Используем методы record, а не getters
        assertEquals(100L, response.userId());
        assertNull(response.role());
    }

    @Test
    void toProjectMemberResponse_shouldMapMemberWithRole() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();
        memberJpa.setProjectId(1L);
        memberJpa.setUserId(100L);
        memberJpa.setRole(ProjectRole.OWNER); // Используем ProjectRole.OWNER

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
        assertEquals(1L, response.projectId());
        assertEquals(100L, response.userId());
        assertEquals("OWNER", response.role()); // Проверяем преобразование через name()
    }

    @Test
    void toProjectMemberResponse_shouldMapSingleMember() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();
        memberJpa.setProjectId(2L);
        memberJpa.setUserId(200L);
        memberJpa.setRole(ProjectRole.EDITOR);

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
        assertEquals(2L, response.projectId());
        assertEquals(200L, response.userId());
        assertEquals("EDITOR", response.role());
    }

    @Test
    void toProjectMemberResponsePage_shouldMapPageOfMembers() {
        // Arrange
        ProjectMemberJpa member1 = new ProjectMemberJpa();
        member1.setProjectId(1L);
        member1.setUserId(101L);
        member1.setRole(ProjectRole.VIEWER);

        ProjectMemberJpa member2 = new ProjectMemberJpa();
        member2.setProjectId(1L);
        member2.setUserId(102L);
        member2.setRole(ProjectRole.EDITOR);

        List<ProjectMemberJpa> content = Arrays.asList(member1, member2);
        Page<ProjectMemberJpa> memberPage = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        Page<ProjectMemberResponse> responsePage = projectMemberMapper.toProjectMemberResponsePage(memberPage);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        assertEquals(0, responsePage.getNumber());
        assertEquals(2, responsePage.getSize());
        assertEquals(2, responsePage.getTotalElements());

        // Проверяем содержимое
        ProjectMemberResponse response1 = responsePage.getContent().get(0);
        assertEquals("VIEWER", response1.role());
        assertEquals(101L, response1.userId());

        ProjectMemberResponse response2 = responsePage.getContent().get(1);
        assertEquals("EDITOR", response2.role());
        assertEquals(102L, response2.userId());
    }

    @Test
    void toProjectMemberResponsePage_shouldHandleEmptyPage() {
        Page<ProjectMemberJpa> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        Page<ProjectMemberResponse> responsePage = projectMemberMapper.toProjectMemberResponsePage(emptyPage);

        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(0, responsePage.getTotalElements());
    }

    @Test
    void toProjectMemberResponse_shouldMapAllProjectRoleValues() {
        // Тестируем все возможные значения ProjectRole
        for (ProjectRole projectRole : ProjectRole.values()) {
            ProjectMemberJpa memberJpa = new ProjectMemberJpa();
            memberJpa.setProjectId(1L);
            memberJpa.setUserId(100L);
            memberJpa.setRole(projectRole);

            ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

            assertNotNull(response);
            assertEquals(projectRole.name(), response.role());
        }
    }

    @Test
    void toProjectMemberResponse_shouldMapMemberWithEditorRole() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();
        memberJpa.setProjectId(3L);
        memberJpa.setUserId(300L);
        memberJpa.setRole(ProjectRole.EDITOR);

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
        assertEquals(3L, response.projectId());
        assertEquals(300L, response.userId());
        assertEquals("EDITOR", response.role());
    }

    @Test
    void toProjectMemberResponse_shouldMapMemberWithViewerRole() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();
        memberJpa.setProjectId(4L);
        memberJpa.setUserId(400L);
        memberJpa.setRole(ProjectRole.VIEWER);

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
        assertEquals(4L, response.projectId());
        assertEquals(400L, response.userId());
        assertEquals("VIEWER", response.role());
    }
}