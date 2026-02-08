package com.example.colaba.task.infrastructure.persistence.mapper;

import com.example.colaba.task.application.dto.comment.CommentResponse;
import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.infrastructure.persistence.entity.CommentJpa;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toDomain(CommentJpa jpa);

    CommentJpa toJpa(Comment domain);

    CommentResponse toResponse(Comment entity);

    default List<CommentResponse> toResponseList(List<Comment> comments) {
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    default Page<CommentResponse> toResponsePage(Page<Comment> comments) {
        return comments.map(this::toResponse);
    }
}