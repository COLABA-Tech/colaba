package com.example.colaba.shared.common.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER", "Service user"),
    ADMIN("ADMIN", "Service administrator");

    private final String value;
    private final String description;

    UserRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static UserRole getDefault() {
        return USER;
    }
}
