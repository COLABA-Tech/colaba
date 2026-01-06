package com.example.colaba.project.mapper;

import com.example.colaba.shared.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.shared.entity.projectmember.ProjectMember;
import com.example.colaba.shared.entity.projectmember.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    ProjectMemberResponse toProjectMemberResponse(ProjectMember member);

    default Page<ProjectMemberResponse> toProjectMemberResponsePage(Page<ProjectMember> members) {
        return members.map(this::toProjectMemberResponse);
    }

    @Named("roleToString")
    default String roleToString(ProjectRole role) {
        return role != null ? role.getValue() : null;
    }
}