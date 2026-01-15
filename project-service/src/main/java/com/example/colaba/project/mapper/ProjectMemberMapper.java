package com.example.colaba.project.mapper;

import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    ProjectMemberResponse toProjectMemberResponse(ProjectMemberJpa member);

    default Page<ProjectMemberResponse> toProjectMemberResponsePage(Page<ProjectMemberJpa> members) {
        return members.map(this::toProjectMemberResponse);
    }
}