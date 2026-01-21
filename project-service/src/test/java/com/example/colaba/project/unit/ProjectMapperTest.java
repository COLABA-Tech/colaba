package com.example.colaba.project.unit;

import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectMapperTest {

    private ProjectMapper projectMapper;

    @BeforeEach
    void setUp() {
        // Since it's a MapStruct mapper, we can get the instance via Mappers
        projectMapper = Mappers.getMapper(ProjectMapper.class);
    }

    @Test
    void toProjectResponse_shouldMapSingleProject() {
        // Arrange
        ProjectJpa projectJpa = new ProjectJpa();
        // Assuming ProjectJpa has setters; adjust if it's immutable or a record
        // projectJpa.setId(1L); // Uncomment and set if id exists
        // projectJpa.setName("Test Project"); // Uncomment if name exists
        projectJpa.setOwnerId(100L);

        // Act
        ProjectResponse response = projectMapper.toProjectResponse(projectJpa);

        // Assert
        assertNotNull(response);
        // If ProjectResponse is a record, use accessor methods like id() instead of getId()
        // assertEquals(1L, response.id()); // Use this if it's a record and id exists
        // assertEquals("Test Project", response.name()); // If name exists
        assertEquals(100L, response.ownerId()); // Assuming ownerId() if record, or getOwnerId() if bean
    }

    @Test
    void toProjectResponseList_shouldMapListOfProjects() {
        // Arrange
        ProjectJpa project1 = new ProjectJpa();
        // project1.setId(1L);
        // project1.setName("Project 1");
        project1.setOwnerId(100L);

        ProjectJpa project2 = new ProjectJpa();
        // project2.setId(2L);
        // project2.setName("Project 2");
        project2.setOwnerId(200L);

        List<ProjectJpa> projects = Arrays.asList(project1, project2);

        // Act
        List<ProjectResponse> responses = projectMapper.toProjectResponseList(projects);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        // assertEquals(1L, responses.get(0).id());
        // assertEquals("Project 1", responses.get(0).name());
        assertEquals(100L, responses.get(0).ownerId());
        // assertEquals(2L, responses.get(1).id());
        // assertEquals("Project 2", responses.get(1).name());
        assertEquals(200L, responses.get(1).ownerId());
    }

    @Test
    void toProjectResponseList_shouldHandleEmptyList() {
        // Arrange
        List<ProjectJpa> emptyList = List.of();

        // Act
        List<ProjectResponse> responses = projectMapper.toProjectResponseList(emptyList);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void toProjectResponsePage_shouldMapPageOfProjects() {
        // Arrange
        ProjectJpa project1 = new ProjectJpa();
        // project1.setId(1L);
        // project1.setName("Project 1");
        project1.setOwnerId(100L);

        ProjectJpa project2 = new ProjectJpa();
        // project2.setId(2L);
        // project2.setName("Project 2");
        project2.setOwnerId(200L);

        List<ProjectJpa> content = Arrays.asList(project1, project2);
        Page<ProjectJpa> projectPage = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        // Act
        Page<ProjectResponse> responsePage = projectMapper.toProjectResponsePage(projectPage);

        // Assert
        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        // assertEquals(1L, responsePage.getContent().get(0).id());
        // assertEquals("Project 1", responsePage.getContent().get(0).name());
        assertEquals(100L, responsePage.getContent().get(0).ownerId());
        // assertEquals(2L, responsePage.getContent().get(1).id());
        // assertEquals("Project 2", responsePage.getContent().get(1).name());
        assertEquals(200L, responsePage.getContent().get(1).ownerId());
        assertEquals(0, responsePage.getNumber());
        assertEquals(2, responsePage.getSize());
        assertEquals(2, responsePage.getTotalElements());
    }

    @Test
    void toProjectResponsePage_shouldHandleEmptyPage() {
        // Arrange
        Page<ProjectJpa> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // Act
        Page<ProjectResponse> responsePage = projectMapper.toProjectResponsePage(emptyPage);

        // Assert
        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(0, responsePage.getTotalElements());
    }
}