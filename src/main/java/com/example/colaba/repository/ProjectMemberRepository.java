package com.example.colaba.repository;

import com.example.colaba.entity.projectmember.ProjectMember;
import com.example.colaba.entity.projectmember.ProjectMemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {
    Page<ProjectMember> findByProjectId(Long projectId, Pageable pageable);
}
