package com.example.colaba.mapper.project;

import com.example.colaba.dto.project.ProjectResponse;
import com.example.colaba.entity.Project;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

public interface ProjectMapper {

    // Конвертация одной сущности в DTO
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
