package com.example.colaba.project.controller;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class ProjectInternalController {

    private final ProjectRepository projectRepository;

    @GetMapping("/owner")
    public List<Project> findByOwner(@RequestBody UserJpa owner) {
        return projectRepository.findByOwner(owner);
    }

    @DeleteMapping("/all")
    public void deleteAll() {
        projectRepository.deleteAll();
    }
}
