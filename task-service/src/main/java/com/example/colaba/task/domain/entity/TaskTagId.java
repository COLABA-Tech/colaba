package com.example.colaba.task.domain.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TaskTagId implements Serializable {
    private Long taskId;
    private Long tagId;
}
