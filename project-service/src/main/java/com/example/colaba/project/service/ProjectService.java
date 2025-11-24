package com.example.colaba.project.service;

import com.example.colaba.project.entity.Project;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.dto.project.CreateProjectRequest;
import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.dto.project.ProjectScrollResponse;
import com.example.colaba.shared.dto.project.UpdateProjectRequest;
import com.example.colaba.shared.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.user.entity.User;
import com.example.colaba.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new UserNotFoundException(request.ownerId()));

        if (projectRepository.existsByName(request.name())) {
            throw new DuplicateProjectNameException(request.name());
        }

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        return projectMapper.toProjectResponse(saved);
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = getProjectEntityById(id);
        return projectMapper.toProjectResponse(project);
    }

    public Project getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        Page<Project> projects = projectRepository.findAll(pageable);
        return projectMapper.toProjectResponsePage(projects);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
        Project project = getProjectEntityById(id);
        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank() && !request.name().equals(project.getName())) {
            if (projectRepository.existsByNameAndIdNot(request.name(), id)) {
                throw new DuplicateProjectNameException(request.name());
            }
            project.setName(request.name());
            hasChanges = true;
        }

        if (request.description() != null && !request.description().equals(project.getDescription())) {
            project.setDescription(request.description());
            hasChanges = true;
        }

        Project saved = hasChanges ? projectRepository.save(project) : project;
        return projectMapper.toProjectResponse(saved);
    }

    @Transactional
    public ProjectResponse changeProjectOwner(Long projectId, Long newOwnerId) {
        Project project = getProjectEntityById(projectId);
        User newOwner = userRepository.findById(newOwnerId)
                .orElseThrow(() -> new UserNotFoundException(newOwnerId));

        project.setOwner(newOwner);
        Project saved = projectRepository.save(project);
        return projectMapper.toProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectRepository.deleteById(id);
    }

    public List<ProjectResponse> getProjectsByOwnerId(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new UserNotFoundException(ownerId));

        List<Project> projects = projectRepository.findByOwner(owner);
        return projectMapper.toProjectResponseList(projects);
    }

    public List<ProjectResponse> getAll() {
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toProjectResponseList(projects);
    }

    public ProjectResponse getById(Long id) {
        return getProjectById(id);
    }

    @Transactional
    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        return updateProject(id, request);
    }

    public List<ProjectResponse> getByOwnerId(Long ownerId) {
        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException(ownerId);
        }

        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projectMapper.toProjectResponseList(projects);
    }

    public ProjectScrollResponse scroll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectRepository.findAll(pageable);

        List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
        boolean hasNext = projectPage.hasNext();
        long total = projectPage.getTotalElements();

        return new ProjectScrollResponse(projects, hasNext, total);
    }
}
