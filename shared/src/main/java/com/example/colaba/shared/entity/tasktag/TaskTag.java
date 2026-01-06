package com.example.colaba.shared.entity.tasktag;

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
public class TaskTag {
    @Id
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
