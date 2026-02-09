package com.example.colaba.task.infrastructure.persistence.mapper;

import com.example.colaba.task.application.dto.task.TaskResponse;
import com.example.colaba.task.domain.entity.Task;
import com.example.colaba.task.infrastructure.persistence.entity.TaskJpa;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    Task toDomain(TaskJpa jpa);

    TaskJpa toJpa(Task domain);

    TaskResponse toTaskResponse(Task task);

    default Page<TaskResponse> toTaskResponsePage(Page<Task> domainPage) {
        return domainPage.map(this::toTaskResponse);
    }
}
