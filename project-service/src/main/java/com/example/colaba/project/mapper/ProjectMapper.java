package com.example.colaba.project.mapper;

import com.example.colaba.project.entity.Project;
import com.example.colaba.shared.dto.project.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.username", target = "ownerName")
    ProjectResponse toProjectResponse(Project project);

    default List<ProjectResponse> toProjectResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    default Page<ProjectResponse> toProjectResponsePage(Page<Project> projects) {
        return projects.map(this::toProjectResponse);
    }
}
