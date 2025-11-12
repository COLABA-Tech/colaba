package com.example.colaba.mapper;

import com.example.colaba.dto.comment.CommentResponse;
import com.example.colaba.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public Page<CommentResponse> toResponsePage(Page<Comment> comments) {
        return comments.map(this::toResponse);
    }

    public Slice<CommentResponse> toResponseSlice(Slice<Comment> comments) {
        return comments.map(this::toResponse);
    }

    public List<CommentResponse> toResponseList(List<Comment> comments) {
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }
}