package com.example.colaba.project.service;

import com.example.colaba.project.dto.project.CreateProjectRequest;
import com.example.colaba.project.dto.project.UpdateProjectRequest;
import com.example.colaba.project.entity.ProjectJpa;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.mapper.ProjectMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.project.repository.ProjectRepository;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.common.dto.project.ProjectResponse;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.common.exception.project.DuplicateProjectNameException;
import com.example.colaba.shared.common.exception.project.ProjectNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
import com.example.colaba.shared.webflux.circuit.UserServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
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
    private final TransactionTemplate transactionTemplate;

    public Mono<ProjectResponse> createProject(CreateProjectRequest request) {
        return userServiceClient.userExists(request.ownerId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException(request.ownerId()));
                    }
                    return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
                        if (projectRepository.existsByName(request.name())) {
                            throw new DuplicateProjectNameException(request.name());
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
                    })).subscribeOn(Schedulers.boundedElastic());
                });
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

    public Mono<ProjectResponse> updateProject(Long id, UpdateProjectRequest request) {
        Mono<Boolean> ownerCheck = (request.ownerId() != null)
                ? userServiceClient.userExists(request.ownerId())
                : Mono.just(true);

        return ownerCheck
                .flatMap(ownerExists -> {
                    if (request.ownerId() != null && !ownerExists) {
                        return Mono.error(new UserNotFoundException(request.ownerId()));
                    }
                    return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
                        ProjectJpa project = projectRepository.findById(id)
                                .orElseThrow(() -> new ProjectNotFoundException(id));

                        boolean hasChanges = false;

                        if (request.name() != null && !request.name().isBlank()
                                && !request.name().equals(project.getName())) {
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
                            projectMemberRepository.updateRole(id, project.getOwnerId(), ProjectRole.EDITOR);
                            project.setOwnerId(request.ownerId());

                            if (projectMemberRepository.existsByProjectIdAndUserId(id, request.ownerId())) {
                                projectMemberRepository.updateRole(id, request.ownerId(), ProjectRole.OWNER);
                            } else {
                                ProjectMemberJpa newOwnerMember = ProjectMemberJpa.builder()
                                        .projectId(id)
                                        .userId(request.ownerId())
                                        .role(ProjectRole.OWNER)
                                        .build();
                                projectMemberRepository.save(newOwnerMember);
                            }
                            hasChanges = true;
                        }

                        ProjectJpa saved = hasChanges ? projectRepository.save(project) : project;
                        return projectMapper.toProjectResponse(saved);
                    })).subscribeOn(Schedulers.boundedElastic());
                });
    }

    public Mono<ProjectResponse> changeProjectOwner(Long projectId, Long newOwnerId) {
        return userServiceClient.userExists(newOwnerId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException(newOwnerId));
                    }
                    return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
                        ProjectJpa project = projectRepository.findById(projectId)
                                .orElseThrow(() -> new ProjectNotFoundException(projectId));

                        if (newOwnerId.equals(project.getOwnerId())) {
                            return projectMapper.toProjectResponse(project);
                        }

                        projectMemberRepository.updateRole(projectId, project.getOwnerId(), ProjectRole.EDITOR);
                        project.setOwnerId(newOwnerId);
                        ProjectJpa saved = projectRepository.save(project);

                        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, newOwnerId)) {
                            projectMemberRepository.updateRole(projectId, newOwnerId, ProjectRole.OWNER);
                        } else {
                            ProjectMemberJpa newOwnerMember = ProjectMemberJpa.builder()
                                    .projectId(projectId)
                                    .userId(newOwnerId)
                                    .role(ProjectRole.OWNER)
                                    .build();
                            projectMemberRepository.save(newOwnerMember);
                        }

                        return projectMapper.toProjectResponse(saved);
                    })).subscribeOn(Schedulers.boundedElastic());
                });
    }

    public Mono<Void> deleteProject(Long id) {
        return Mono.fromCallable(() -> {
                    if (!projectRepository.existsById(id)) {
                        throw new ProjectNotFoundException(id);
                    }
                    return id;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(_ -> taskServiceClient.deleteTasksByProject(id))
                .then(Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(_ -> {
                    projectMemberRepository.deleteByProjectId(id);
                    tagRepository.deleteByProjectId(id);
                    projectRepository.deleteById(id);
                })).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }

    public Mono<List<ProjectResponse>> getProjectsByOwnerId(Long ownerId) {
        return userServiceClient.userExists(ownerId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException(ownerId));
                    }
                    return Mono.fromCallable(() ->
                                    projectRepository.findByOwnerId(ownerId)
                            )
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(projectMapper::toProjectResponseList);
                });
    }

    public Mono<Void> handleUserDeletion(Long userId) {
        return Mono.fromCallable(() -> projectRepository.findByOwnerId(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .concatMap(projectJpa -> this.deleteProject(projectJpa.getId()))
                .then()
                .then(Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(_ ->
                        projectMemberRepository.deleteByUserId(userId)
                )).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
}