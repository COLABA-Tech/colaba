package com.example.colaba.project.service;

import com.example.colaba.project.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.project.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.project.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.project.entity.projectmember.ProjectMemberId;
import com.example.colaba.project.entity.projectmember.ProjectMemberJpa;
import com.example.colaba.project.mapper.ProjectMemberMapper;
import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.shared.common.entity.ProjectRole;
import com.example.colaba.shared.common.exception.projectmember.DuplicateProjectMemberException;
import com.example.colaba.shared.common.exception.projectmember.ProjectMemberNotFoundException;
import com.example.colaba.shared.common.exception.user.UserNotFoundException;
import com.example.colaba.shared.webflux.circuit.UserServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserServiceClientWrapper userServiceClient;
    private final ProjectMemberMapper projectMemberMapper;

    public Mono<Page<ProjectMemberResponse>> getMembersByProject(Long projectId, Pageable pageable) {
        return projectService.getProjectEntityById(projectId)
                .flatMap(_ -> Mono.fromCallable(() ->
                        projectMemberMapper.toProjectMemberResponsePage(
                                projectMemberRepository.findByProjectId(projectId, pageable)
                        )
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    @Transactional
    public Mono<ProjectMemberResponse> createMembership(Long projectId, CreateProjectMemberRequest request) {
        return projectService.getProjectEntityById(projectId)
                .flatMap(_ -> Mono.fromCallable(() -> userServiceClient.userExists(request.userId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(userExists -> {
                            if (!userExists) {
                                return Mono.error(new UserNotFoundException(request.userId()));
                            }
                            return Mono.fromCallable(() -> {
                                ProjectMemberId id = new ProjectMemberId(projectId, request.userId());
                                if (projectMemberRepository.existsById(id)) {
                                    throw new DuplicateProjectMemberException(request.userId(), projectId);
                                }
                                ProjectMemberJpa member = ProjectMemberJpa.builder()
                                        .projectId(projectId)
                                        .userId(request.userId())
                                        .role(request.role() != null ? request.role() : ProjectRole.getDefault())
                                        .build();
                                ProjectMemberJpa saved = projectMemberRepository.save(member);
                                return projectMemberMapper.toProjectMemberResponse(saved);
                            }).subscribeOn(Schedulers.boundedElastic());
                        }));
    }

    @Transactional
    public Mono<ProjectMemberResponse> updateMembership(Long projectId, Long userId, UpdateProjectMemberRequest request) {
        return Mono.fromCallable(() -> {
                    ProjectMemberId id = new ProjectMemberId(projectId, userId);
                    return projectMemberRepository.findById(id);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalMember -> {
                    if (optionalMember.isEmpty()) {
                        return Mono.error(new ProjectMemberNotFoundException(projectId, userId));
                    }

                    ProjectMemberJpa member = optionalMember.get();
                    boolean hasChanges = false;

                    if (request.role() != null && !request.role().equals(member.getRole())) {
                        member.setRole(request.role());
                        hasChanges = true;
                    }

                    if (hasChanges) {
                        return Mono.fromCallable(() -> projectMemberRepository.save(member))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(projectMemberMapper::toProjectMemberResponse);
                    } else {
                        return Mono.just(projectMemberMapper.toProjectMemberResponse(member));
                    }
                });
    }

    @Transactional
    public Mono<Void> deleteMembership(Long projectId, Long userId) {
        return Mono.fromCallable(() -> {
                    ProjectMemberId id = new ProjectMemberId(projectId, userId);
                    return projectMemberRepository.existsById(id);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ProjectMemberNotFoundException(projectId, userId));
                    }
                    return Mono.fromRunnable(() ->
                                    projectMemberRepository.deleteById(new ProjectMemberId(projectId, userId)))
                            .subscribeOn(Schedulers.boundedElastic());
                }).then();
    }
}