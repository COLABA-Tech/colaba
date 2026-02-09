package com.example.colaba.task.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TaskTag {
    private Long taskId;
    private Long tagId;
}
