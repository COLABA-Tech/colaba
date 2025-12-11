package com.example.colaba.project.service;

import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.shared.dto.project.*;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.exception.user.UserNotFoundException;
import com.example.colaba.shared.mapper.ProjectMapper;
import com.example.colaba.shared.mapper.UserMapper;
import feign.FeignException;
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
    private final UserClientWrapper userClientWrapper;   // ← Через обёртку с Circuit Breaker
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;

    @Transactional
    public Mono<ProjectResponse> createProject(CreateProjectRequest request) {
        return Mono.fromCallable(() -> {
            if (projectRepository.existsByName(request.name())) {
                throw new DuplicateProjectNameException(request.name());
            }

            User owner;
            try {
                owner = userClientWrapper.getUserEntityById(request.ownerId());
            } catch (FeignException.NotFound e) {
                throw new UserNotFoundException(request.ownerId());
            }

            UserJpa ownerJpa = userMapper.toUserJpa(owner);
            Project project = Project.builder()
                    .name(request.name())
                    .description(request.description())
                    .owner(ownerJpa)
                    .build();

            Project saved = projectRepository.save(project);
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<ProjectResponse> getProjectById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .map(projectMapper::toProjectResponse)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Project> getProjectEntityById(Long id) {
        return Mono.fromCallable(() -> projectRepository.findById(id)
                        .orElseThrow(() -> new ProjectNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<ProjectResponse>> getAllProjects() {
        return Mono.fromCallable(projectRepository::findAll)
                .map(projectMapper::toProjectResponseList)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request) {
        return Mono.fromCallable(() -> {
            Project project = projectRepository.findById(id)
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

            if (request.ownerId() != null && !request.ownerId().equals(project.getOwner().getId())) {
                User newOwner;
                try {
                    newOwner = userClientWrapper.getUserEntityById(request.ownerId());
                } catch (FeignException.NotFound e) {
                    throw new UserNotFoundException(request.ownerId());
                }
                project.setOwner(userMapper.toUserJpa(newOwner));
                hasChanges = true;
            }

            Project saved = hasChanges ? projectRepository.save(project) : project;
            return projectMapper.toProjectResponse(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId) {
        return getProjectEntityById(projectId)
                .zipWhen(project -> Mono.fromCallable(() -> userClientWrapper.getUserEntityById(newOwnerId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .onErrorMap(FeignException.NotFound.class,
                                ex -> new UserNotFoundException(newOwnerId))
                )
                .flatMap(tuple -> {
                    Project project = tuple.getT1();
                    User user = tuple.getT2();
                    project.setOwner(userMapper.toUserJpa(user));

                    return Mono.fromCallable(() -> projectRepository.save(project))
                            .map(projectMapper::toProjectResponse)
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Transactional
    public Mono<Void> deleteProject(Long id) {
        return Mono.fromRunnable(() -> {
            if (!projectRepository.existsById(id)) {
                throw new ProjectNotFoundException(id);
            }
            projectRepository.deleteById(id);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<List<ProjectResponse>> getProjectByOwnerId(Long ownerId) {
        return Mono.fromCallable(() -> {
                    User user;
                    try {
                        user = userClientWrapper.getUserEntityById(ownerId);
                    } catch (FeignException.NotFound e) {
                        throw new UserNotFoundException(ownerId);
                    }
                    UserJpa userJpa = userMapper.toUserJpa(user);
                    return projectRepository.findByOwner(userJpa);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(projectMapper::toProjectResponseList);
    }

    public Mono<ProjectScrollResponse> scroll(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            Page<Project> projectPage = projectRepository.findAll(pageable);
            List<ProjectResponse> projects = projectMapper.toProjectResponseList(projectPage.getContent());
            return new ProjectScrollResponse(projects, projectPage.hasNext(), projectPage.getTotalElements());
        }).subscribeOn(Schedulers.boundedElastic());
    }
}