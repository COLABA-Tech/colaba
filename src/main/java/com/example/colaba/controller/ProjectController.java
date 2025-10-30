package com.example.colaba.controller;

import com.example.colaba.dto.CreateProjectRequest;
import com.example.colaba.dto.UpdateProjectRequest;
import com.example.colaba.dto.ProjectResponse;
import com.example.colaba.dto.ProjectScrollResponse;
import com.example.colaba.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller для Project, использующий Request/Response DTO.
 *
 * Предполагает, что ProjectService имеет методы примерно такие:
 * - ProjectResponse create(CreateProjectRequest request);
 * - ProjectResponse update(Long id, UpdateProjectRequest request);
 * - ProjectResponse getById(Long id);
 * - List<ProjectResponse> getAll();
 * - List<ProjectResponse> getByOwnerId(Long ownerId);
 * - ProjectScrollResponse scroll(int page, int size);
 *
 * Если сигнатуры сервиса другие — немного подправь вызовы ниже.
 */
@Validated
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Создать проект
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse created = projectService.create(request);
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
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
