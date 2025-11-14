package com.example.colaba.mapper;

import com.example.colaba.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.entity.projectmember.ProjectMember;
import com.example.colaba.entity.projectmember.ProjectRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "userUsername")
//    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    @Mapping(source = "role", target = "role")
    ProjectMemberResponse toProjectMemberResponse(ProjectMember member);

    default Page<ProjectMemberResponse> toProjectMemberResponsePage(Page<ProjectMember> members) {
        return members.map(this::toProjectMemberResponse);
    }

    @Named("roleToString")
    default String roleToString(ProjectRole role) {
        return role != null ? role.getValue() : null;
    }
}