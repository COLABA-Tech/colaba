package com.example.colaba.mapper;

import com.example.colaba.dto.task.TaskResponse;
import com.example.colaba.entity.task.Task;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskResponse toTaskResponse(Task task);

    List<TaskResponse> toUserTaskList(List<Task> users);

    default Page<TaskResponse> toTaskResponsePage(Page<Task> tasks) {
        return tasks.map(this::toTaskResponse);
    }
}
