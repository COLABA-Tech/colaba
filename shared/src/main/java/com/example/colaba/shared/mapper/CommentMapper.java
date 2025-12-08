package com.example.colaba.shared.mapper;

import com.example.colaba.shared.dto.comment.CommentResponse;
import com.example.colaba.shared.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment entity) {
        return new CommentResponse(
                entity.getId(),
                entity.getTask().getId(),
                entity.getUser().getId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Page<CommentResponse> toResponsePage(Page<Comment> comments) {
        return comments.map(this::toResponse);
    }

    public List<CommentResponse> toResponseList(List<Comment> comments) {
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }
}