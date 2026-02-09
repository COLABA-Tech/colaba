package com.example.colaba.task.application.mapper;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.task.application.dto.task.TaskResponse;
import com.example.colaba.task.domain.entity.Task;

import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getProjectId(),
                task.getAssigneeId(),
                task.getReporterId(),
                task.getDueDate()
        );
    }

    public PagedResult<TaskResponse> toResponsePage(PagedResult<Task> tasks) {
        if (tasks == null) {
            return null;
        }
        List<TaskResponse> responseList = tasks.content().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResult<>(
                responseList,
                tasks.totalElements(),
                tasks.totalPages(),
                tasks.currentPage(),
                tasks.size()
        );
    }
}
