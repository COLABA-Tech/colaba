package com.example.colaba.task.mapper;

import com.example.colaba.shared.dto.task.TaskResponse;
import com.example.colaba.shared.entity.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "assignee.username", target = "assigneeUsername")
    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.username", target = "reporterUsername")
    TaskResponse toTaskResponse(Task task);

    default Page<TaskResponse> toTaskResponsePage(Page<Task> tasks) {
        return tasks.map(this::toTaskResponse);
    }
}
