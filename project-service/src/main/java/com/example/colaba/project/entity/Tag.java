package com.example.colaba.project.entity;

import com.example.colaba.task.entity.task.Task;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "project_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"tasks"})
@EqualsAndHashCode(exclude = {"tasks"})
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name cannot be blank")
    @Size(max = 20, message = "Tag name must not exceed 20 characters")
    @Column(nullable = false, length = 20)
    private String name;

    @NotNull(message = "Project is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    @ManyToMany(mappedBy = "tags")
    private Set<Task> tasks = new HashSet<>();
}