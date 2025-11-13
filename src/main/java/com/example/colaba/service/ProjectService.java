package com.example.colaba.service;

import com.example.colaba.dto.project.CreateProjectRequest;
import com.example.colaba.dto.project.ProjectResponse;
import com.example.colaba.dto.project.ProjectScrollResponse;
import com.example.colaba.dto.project.UpdateProjectRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import com.example.colaba.exception.project.DuplicateProjectNameException;
import com.example.colaba.exception.project.ProjectNotFoundException;
import com.example.colaba.exception.user.UserNotFoundException;
import com.example.colaba.mapper.project.ProjectMapper;
import com.example.colaba.repository.ProjectRepository;
import com.example.colaba.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        if (projectRepository.existsByName(request.name())) {
            throw new DuplicateProjectNameException(request.name());
        }

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .build();

        Project saved = projectRepository.save(project);
        return projectMapper.toProjectResponse(saved); // используется маппер
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = getProjectEntityById(id);
        return projectMapper.toProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projectMapper.toProjectResponsePage(projects);
    }

    @Transactional(readOnly = true)
    public ProjectScrollResponse getProjectsScroll(String cursor, int limit) {
        // Определяем смещение по курсору
        long offset = cursor == null || cursor.isBlank() ? 0 : Long.parseLong(cursor);

        // PageRequest требует pageNumber и pageSize
        // Здесь pageNumber = offset / limit
        int pageNumber = (int) (offset / limit);
        Pageable pageable = PageRequest.of(pageNumber, limit);

        Slice<Project> slice = projectRepository.findAllBy(pageable);

        // Конвертация в DTO
        List<ProjectResponse> content = projectMapper.toProjectResponseList(slice.getContent());

        // Новый курсор = текущее смещение + количество элементов в Slice
        String nextCursor = String.valueOf(offset + slice.getNumberOfElements());

        boolean hasMore = slice.hasNext();

        return new ProjectScrollResponse(content, nextCursor, hasMore);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = getProjectEntityById(id);
        boolean hasChanges = false;

        if (request.getName() != null && !request.getName().isBlank() && !request.getName().equals(project.getName())) {
            if (projectRepository.existsByNameAndIdNot(request.getName(), id)) {
                throw new DuplicateProjectNameException(request.getName());
            }
            project.setName(request.getName());
            hasChanges = true;
        }

        if (request.getDescription() != null && !request.getDescription().equals(project.getDescription())) {
            project.setDescription(request.getDescription());
            hasChanges = true;
        }


        Project saved = hasChanges ? projectRepository.save(project) : project;
        return projectMapper.toProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByOwnerId(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        List<Project> projects = projectRepository.findByOwner(owner);
        return projectMapper.toProjectResponseList(projects);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAll() {
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toProjectResponseList(projects);
    }
    @Transactional(readOnly = true)
    public ProjectResponse getById(Long id) {
        return getProjectById(id); // твой существующий метод
    }

    @Transactional
    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        return updateProject(id, request); // твой существующий метод
    }
    @Transactional(readOnly = true)
    public List<ProjectResponse> getByOwnerId(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projectMapper.toProjectResponseList(projects);
    }
    @Transactional(readOnly = true)
    public ProjectScrollResponse scroll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectRepository.findAll(pageable);

        List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
        boolean hasNext = projectPage.hasNext();
        long total = projectPage.getTotalElements();

        return new ProjectScrollResponse(projects, hasNext, total);
    }



}
