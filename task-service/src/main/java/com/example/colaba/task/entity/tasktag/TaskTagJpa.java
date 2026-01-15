package com.example.colaba.task.entity.tasktag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_tags")
@IdClass(TaskTagId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class TaskTagJpa {
    @Id
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
