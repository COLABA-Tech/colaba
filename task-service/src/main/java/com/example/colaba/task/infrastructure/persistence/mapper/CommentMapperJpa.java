package com.example.colaba.task.infrastructure.persistence.mapper;

import com.example.colaba.task.domain.entity.Comment;
import com.example.colaba.task.infrastructure.persistence.entity.CommentJpa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapperJpa {
    Comment toDomain(CommentJpa jpa);

    CommentJpa toJpa(Comment domain);
}