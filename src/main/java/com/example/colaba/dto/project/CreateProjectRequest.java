package com.example.colaba.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateProjectRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long ownerId;

    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String description, Long ownerId) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Long ownerId() {
        return ownerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
}
