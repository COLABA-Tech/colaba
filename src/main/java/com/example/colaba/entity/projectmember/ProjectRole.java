package com.example.colaba.entity.projectmember;

import lombok.Getter;

@Getter
public enum ProjectRole {
    OWNER("OWNER", "Project owner with full permissions"),
    MEMBER("MEMBER", "Project member with edit permissions"),
    VIEWER("VIEWER", "Project viewer with read-only permissions");

    private final String value;
    private final String description;

    ProjectRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ProjectRole getDefault() {
        return VIEWER;
    }
}
