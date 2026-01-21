package com.example.colaba.project.unit;

import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
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
    void toProjectMemberResponse_shouldMapSingleMember() {
        ProjectMemberJpa memberJpa = new ProjectMemberJpa();

        ProjectMemberResponse response = projectMemberMapper.toProjectMemberResponse(memberJpa);

        assertNotNull(response);
    }

    @Test
    void toProjectMemberResponsePage_shouldMapPageOfMembers() {
        // Arrange
        ProjectMemberJpa member1 = new ProjectMemberJpa();

        ProjectMemberJpa member2 = new ProjectMemberJpa();
        List<ProjectMemberJpa> content = Arrays.asList(member1, member2);
        Page<ProjectMemberJpa> memberPage = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        Page<ProjectMemberResponse> responsePage = projectMemberMapper.toProjectMemberResponsePage(memberPage);

        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        assertEquals(0, responsePage.getNumber());
        assertEquals(2, responsePage.getSize());
        assertEquals(2, responsePage.getTotalElements());
    }

    @Test
    void toProjectMemberResponsePage_shouldHandleEmptyPage() {
        Page<ProjectMemberJpa> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        Page<ProjectMemberResponse> responsePage = projectMemberMapper.toProjectMemberResponsePage(emptyPage);

        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(0, responsePage.getTotalElements());
    }
}
