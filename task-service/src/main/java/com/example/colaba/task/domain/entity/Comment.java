package com.example.colaba.task.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
public class Comment {
    private Long id;
    private Long taskId;
    private Long userId;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
