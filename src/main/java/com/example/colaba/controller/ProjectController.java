package com.example.colaba.controller;

import com.example.colaba.dto.project.CreateProjectRequest;
import com.example.colaba.dto.project.UpdateProjectRequest;
import com.example.colaba.dto.project.ProjectResponse;
import com.example.colaba.dto.project.ProjectScrollResponse;
import com.example.colaba.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Создать проект
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.createProject(request);
        return ResponseEntity.ok(created);
    }


    /**
     * Обновить проект
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody UpdateProjectRequest request) {
        ProjectResponse updated = projectService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Получить проект по id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable("id") Long id) {
        ProjectResponse response = projectService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Получить все проекты (без пагинации)
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAll() {
        List<ProjectResponse> list = projectService.getAll();
        return ResponseEntity.ok(list);
    }

    /**
     * Получить проекты по владельцу
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ProjectResponse>> getByOwner(@PathVariable("ownerId") Long ownerId) {
        List<ProjectResponse> list = projectService.getByOwnerId(ownerId);
        return ResponseEntity.ok(list);
    }

    /**
     * Пагинация / прокрутка (scroll). Параметры page/size опциональны.
     * Возвращает ProjectScrollResponse (list + hasNext + total).
     */
    @GetMapping("/scroll")
    public ResponseEntity<ProjectScrollResponse> scroll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "20") int size
    ) {
        ProjectScrollResponse scroll = projectService.scroll(page, size);
        return ResponseEntity.ok(scroll);
    }

    /**
     * Удалить проект
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        projectService.deleteProject(id); // <-- использовать правильное имя метода
        return ResponseEntity.noContent().build();
    }

}
