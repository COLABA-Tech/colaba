package com.example.colaba.mapper.project;

import com.example.colaba.dto.project.ProjectResponse;
import com.example.colaba.entity.Project;
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

    // Конвертация списка сущностей в список DTO
    default List<ProjectResponse> toProjectResponseList(List<Project> projects) {
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    // Конвертация Page<Project> в Page<ProjectResponse>
    default Page<ProjectResponse> toProjectResponsePage(Page<Project> projects) {
        return projects.map(this::toProjectResponse);
    }
}
