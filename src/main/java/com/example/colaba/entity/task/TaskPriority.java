package com.example.colaba.entity.task;

import lombok.Getter;

@Getter
public enum TaskPriority {
    LOW("Low", "Low priority task", 1),
    MEDIUM("Medium", "Medium priority task", 2),
    HIGH("High", "High priority task", 3),
    URGENT("Urgent", "Urgent priority task", 4);

    private final String name;
    private final String description;
    private final int weight;

    TaskPriority(String displayName, String description, int weight) {
        this.name = displayName;
        this.description = description;
        this.weight = weight;
    }
}