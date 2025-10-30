package com.example.colaba.service;

import com.example.colaba.dto.CreateProjectRequest;
import com.example.colaba.dto.UpdateProjectRequest;
import com.example.colaba.dto.ProjectResponse;
import com.example.colaba.dto.ProjectScrollResponse;

import java.util.List;

/**
 * Сервис для управления проектами.
 * Использует DTO: CreateProjectRequest / UpdateProjectRequest / ProjectResponse.
 */
public interface ProjectService {

    /**
     * Создать новый проект.
     */
    ProjectResponse create(CreateProjectRequest request);

    /**
     * Обновить проект по id.
     */
    ProjectResponse update(Long id, UpdateProjectRequest request);

    /**
     * Получить проект по id.
     */
    ProjectResponse getById(Long id);

    /**
     * Получить список всех проектов.
     */
    List<ProjectResponse> getAll();

    /**
     * Получить проекты по владельцу.
     */
    List<ProjectResponse> getByOwnerId(Long ownerId);

    /**
     * Пагинация/прокрутка — если нужна поддержка скролла.
     */
    ProjectScrollResponse scroll(int page, int size);

    /**
     * Удалить проект.
     */
    void delete(Long id);
}

