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
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserServiceClientWrapper userServiceClient;
    private final ProjectMemberMapper projectMemberMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<Page<ProjectMemberResponse>> getMembersByProject(Long projectId, Pageable pageable) {
        return projectService.getProjectEntityById(projectId)
                .then(Mono.fromCallable(() ->
                        projectMemberMapper.toProjectMemberResponsePage(
                                projectMemberRepository.findByProjectId(projectId, pageable)
                        )
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<ProjectMemberResponse> createMembership(Long projectId, CreateProjectMemberRequest request) {
        return projectService.getProjectEntityById(projectId)
                .flatMap(_ -> userServiceClient.userExists(request.userId()))
                .flatMap(userExists -> {
                    if (!userExists) {
                        return Mono.error(new UserNotFoundException(request.userId()));
                    }
                    return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
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
                    })).subscribeOn(Schedulers.boundedElastic());
                });
    }

    public Mono<ProjectMemberResponse> updateMembership(Long projectId, Long userId, UpdateProjectMemberRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
            ProjectMemberId id = new ProjectMemberId(projectId, userId);
            ProjectMemberJpa member = projectMemberRepository.findById(id)
                    .orElseThrow(() -> new ProjectMemberNotFoundException(projectId, userId));

            boolean hasChanges = false;

            if (request.role() != null && !request.role().equals(member.getRole())) {
                member.setRole(request.role());
                hasChanges = true;
            }

            ProjectMemberJpa saved = hasChanges ? projectMemberRepository.save(member) : member;
            return projectMemberMapper.toProjectMemberResponse(saved);
        })).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteMembership(Long projectId, Long userId) {
        return Mono.fromCallable(() -> {
                    ProjectMemberId id = new ProjectMemberId(projectId, userId);
                    if (!projectMemberRepository.existsById(id)) {
                        throw new ProjectMemberNotFoundException(projectId, userId);
                    }
                    return id;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(_ ->
                        projectMemberRepository.deleteById(new ProjectMemberId(projectId, userId))
                )).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
}