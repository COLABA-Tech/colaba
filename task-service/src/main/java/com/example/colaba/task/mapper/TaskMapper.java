package com.example.colaba.task.mapper;

import com.example.colaba.task.dto.task.TaskResponse;
import com.example.colaba.task.entity.task.TaskJpa;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskResponse toTaskResponse(TaskJpa task);

    default Page<TaskResponse> toTaskResponsePage(Page<TaskJpa> tasks) {
        return tasks.map(this::toTaskResponse);
    }
}
