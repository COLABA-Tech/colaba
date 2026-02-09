package com.example.colaba.task.application.mapper;

import com.example.colaba.shared.common.application.dto.common.PagedResult;
import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.domain.entity.Comment;

import java.util.List;
import java.util.stream.Collectors;

public class CommentMapper {
    public CommentResponse toResponse(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentResponse(
                comment.getId(),
                comment.getTaskId(),
                comment.getUserId(),
                comment.getContent()
        );
    }

    public List<CommentResponse> toResponseList(List<Comment> comments) {
        if (comments == null) {
            return null;
        }
        return comments.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PagedResult<CommentResponse> toResponsePage(PagedResult<Comment> comments) {
        List<CommentResponse> responseList = comments.content().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PagedResult<>(
                responseList,
                comments.totalElements(),
                comments.totalPages(),
                comments.currentPage(),
                comments.size()
        );
    }
}
