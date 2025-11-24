package com.example.colaba.project.entity.projectmember;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class ProjectMemberId implements Serializable {
    private Long projectId;
    private Long userId;
}