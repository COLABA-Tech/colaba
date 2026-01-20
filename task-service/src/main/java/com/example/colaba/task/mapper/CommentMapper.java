package com.example.colaba.task.mapper;

import com.example.colaba.task.dto.comment.CommentResponse;
import com.example.colaba.task.entity.CommentJpa;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentResponse toResponse(CommentJpa entity) {
        return new CommentResponse(
                entity.getId(),
                entity.getTaskId(),
                entity.getUserId(),
                entity.getContent()
        );
    }

    public Page<CommentResponse> toResponsePage(Page<CommentJpa> comments) {
        return comments.map(this::toResponse);
    }

    public List<CommentResponse> toResponseList(List<CommentJpa> comments) {
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }
}