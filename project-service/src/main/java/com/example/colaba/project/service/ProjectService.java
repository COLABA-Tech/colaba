package com.example.colaba.project.service;

import com.example.colaba.project.circuit.TaskServiceClientWrapper;
import com.example.colaba.project.circuit.UserServiceClientWrapper;
import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.ProjectScrollResponse;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.entity.projectmember.ProjectRole;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.dto.project.ProjectResponse;
import com.example.colaba.shared.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TagRepository tagRepository;
    private final UserServiceClientWrapper userServiceClient;
    private final TaskServiceClientWrapper taskServiceClient;
    private final ProjectMapper projectMapper;

    @Transactional
    public Mono<ProjectResponse> createProject(CreateProjectRequest request) {
        return Mono.fromCallable(() -> {
            if (projectRepository.existsByName(request.name())) {
                throw new DuplicateProjectNameException(request.name());
            }

            boolean userExists = userServiceClient.userExists(request.ownerId());
            if (!userExists) {
                throw new UserNotFoundException(request.ownerId());
            }

            ProjectJpa project = ProjectJpa.builder()
                    .name(request.name())
                    .description(request.description())
                    .ownerId(request.ownerId())
                    .build();
            ProjectJpa saved = projectRepository.save(project);

            ProjectMemberJpa ownerMember = ProjectMemberJpa.builder()
                    .projectId(saved.getId())
                    .userId(request.ownerId())
                    .role(ProjectRole.OWNER)
                    .build();
            projectMemberRepository.save(ownerMember);

            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectResponse> getProjectById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .map(projectMapper::toProjectResponse)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectJpa> getProjectEntityById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<ProjectResponse>> getAllProjects(Pageable pageable) {
        return Mono.fromCallable(() -> projectMapper.toProjectResponsePage(projectRepository.findAll(pageable)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request) {
        return Mono.fromCallable(() -> {
            ProjectJpa project = projectRepository.findById(id)
                    .orElseThrow(() -> new ProjectNotFoundException(id));

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

            if (request.ownerId() != null && !request.ownerId().equals(project.getOwnerId())) {
                boolean userExists = userServiceClient.userExists(request.ownerId());
                if (!userExists) {
                    throw new UserNotFoundException(request.ownerId());
                }
                project.setOwnerId(request.ownerId());
                hasChanges = true;
            }

            ProjectJpa saved = hasChanges ? projectRepository.save(project) : project;
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId) {
        return Mono.fromCallable(() -> {
            ProjectJpa project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ProjectNotFoundException(projectId));
            if (newOwnerId.equals(project.getOwnerId())) {
                return projectMapper.toProjectResponse(project);
            }
            boolean userExists = userServiceClient.userExists(newOwnerId);
            if (!userExists) {
                throw new UserNotFoundException(newOwnerId);
            }
            project.setOwnerId(newOwnerId);
            ProjectJpa saved = projectRepository.save(project);
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectMemberRepository.deleteByProjectId(id);
        tagRepository.deleteByProjectId(id);
        taskServiceClient.deleteTasksByProject(id);
        projectRepository.deleteById(id);
    }

    public Mono<List<ProjectResponse>> getProjectByOwnerId(Long ownerId) {
        return Mono.fromCallable(() -> {
            boolean userExists = userServiceClient.userExists(ownerId);
            if (!userExists) {
                throw new UserNotFoundException(ownerId);
            }
            List<ProjectJpa> projects = projectRepository.findByOwnerId(ownerId);
            return projectMapper.toProjectResponseList(projects);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectScrollResponse> scroll(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProjectJpa> projectPage = projectRepository.findAll(pageable);

            List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
            boolean hasNext = projectPage.hasNext();
            long total = projectPage.getTotalElements();

            return new ProjectScrollResponse(projects, hasNext, total);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public void handleUserDeletion(Long userId) {
        projectMemberRepository.deleteByUserId(userId);
    }

    public Mono<Boolean> isMember(Long projectId, Long userId) {
        return Mono.fromCallable(() -> {
            return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
