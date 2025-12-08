package com.example.colaba.project.service;

import com.example.colaba.project.repository.ProjectMemberRepository;
import com.example.colaba.shared.client.UserServiceClient;
import com.example.colaba.shared.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.shared.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.shared.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.shared.entity.Project;
import com.example.colaba.shared.entity.UserJpa;
import com.example.colaba.shared.entity.projectmember.ProjectMember;
import com.example.colaba.shared.entity.projectmember.ProjectMemberId;
import com.example.colaba.shared.entity.projectmember.ProjectRole;
import com.example.colaba.shared.exception.projectmember.DuplicateProjectMemberException;
import com.example.colaba.shared.exception.projectmember.ProjectMemberNotFoundException;
import com.example.colaba.shared.mapper.ProjectMemberMapper;
import com.example.colaba.shared.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserServiceClient userServiceClient;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;

    public Mono<Page<ProjectMemberResponse>> getMembersByProject(Long projectId, Pageable pageable) {
        return projectService.getProjectEntityById(projectId)
                .flatMap(_ -> Mono.fromCallable(() ->
                        projectMemberMapper.toProjectMemberResponsePage(
                                projectMemberRepository.findByProjectId(projectId, pageable)
                        )
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<ProjectMemberResponse> createMembership(Long projectId, CreateProjectMemberRequest request) {
        return Mono.zip(
                projectService.getProjectEntityById(projectId),
                Mono.fromCallable(() -> userServiceClient.getUserEntityById(request.userId()))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(user -> Mono.just(userMapper.toUserJpa(user)))
        ).flatMap(tuple -> {
            Project project = tuple.getT1();
            UserJpa user = tuple.getT2();

            return Mono.fromCallable(() -> {
                ProjectMemberId id = new ProjectMemberId(projectId, user.getId());

                if (projectMemberRepository.existsById(id)) {
                    throw new DuplicateProjectMemberException(user.getUsername(), projectId);
                }

                ProjectMember member = ProjectMember.builder()
                        .projectId(project.getId())
                        .userId(user.getId())
                        .project(project)
                        .user(user)
                        .role(request.role() != null ? request.role() : ProjectRole.getDefault())
                        .build();

                ProjectMember saved = projectMemberRepository.save(member);
                return projectMemberMapper.toProjectMemberResponse(saved);
            }).subscribeOn(Schedulers.boundedElastic());
        });
    }

    public Mono<ProjectMemberResponse> updateMembership(Long projectId, Long userId, UpdateProjectMemberRequest request) {
        return Mono.fromCallable(() -> {
                    ProjectMemberId id = new ProjectMemberId(projectId, userId);
                    return projectMemberRepository.findById(id);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalMember -> {
                    if (optionalMember.isEmpty()) {
                        return Mono.error(new ProjectMemberNotFoundException(projectId, userId));
                    }

                    ProjectMember member = optionalMember.get();
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

    public Mono<Void> deleteMembership(Long projectId, Long userId) {
        return Mono.fromCallable(() -> {
                    ProjectMemberId id = new ProjectMemberId(projectId, userId);
                    return projectMemberRepository.existsById(id);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ProjectMemberNotFoundException(projectId, userId));
                    }
                    return Mono.fromRunnable(() -> projectMemberRepository.deleteById(new ProjectMemberId(projectId, userId)))
                            .subscribeOn(Schedulers.boundedElastic());
                }).then();
    }
}