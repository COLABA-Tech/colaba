package com.example.colaba.service;

import com.example.colaba.dto.projectmember.CreateProjectMemberRequest;
import com.example.colaba.dto.projectmember.ProjectMemberResponse;
import com.example.colaba.dto.projectmember.UpdateProjectMemberRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.User;
import com.example.colaba.entity.projectmember.ProjectMember;
import com.example.colaba.entity.projectmember.ProjectMemberId;
import com.example.colaba.entity.projectmember.ProjectRole;
import com.example.colaba.exception.projectmember.DuplicateProjectMemberException;
import com.example.colaba.exception.projectmember.ProjectMemberNotFoundException;
import com.example.colaba.mapper.ProjectMemberMapper;
import com.example.colaba.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMemberMapper projectMemberMapper;

    public Page<ProjectMemberResponse> getMembersByProject(Long projectId, Pageable pageable) {
        Project project = projectService.getProjectEntityById(projectId);
        return projectMemberMapper.toProjectMemberResponsePage(
                projectMemberRepository.findByProjectId(projectId, pageable));
    }

    @Transactional
    public ProjectMemberResponse createMembership(Long projectId, CreateProjectMemberRequest request) {
        Project project = projectService.getProjectEntityById(projectId);
        User user = userService.getUserEntityById(request.userId());

        ProjectMemberId id = new ProjectMemberId(projectId, request.userId());
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
    }

    @Transactional
    public ProjectMemberResponse updateMembership(Long projectId, Long userId, UpdateProjectMemberRequest request) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        ProjectMember member = projectMemberRepository.findById(id)
                .orElseThrow(() -> new ProjectMemberNotFoundException(projectId, userId));

        boolean hasChanges = false;
        if (request.role() != null && !request.role().equals(member.getRole())) {
            member.setRole(request.role());
            hasChanges = true;
        }

        ProjectMember updated = hasChanges ? projectMemberRepository.save(member) : member;
        return projectMemberMapper.toProjectMemberResponse(updated);
    }

    @Transactional
    public void deleteMembership(Long projectId, Long userId) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepository.existsById(id)) {
            throw new ProjectMemberNotFoundException(projectId, userId);
        }
        projectMemberRepository.deleteById(id);
    }
}