package com.example.colaba.shared.entity.tasktag;

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
