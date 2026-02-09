package com.example.colaba.shared.webmvc.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.shared.webmvc.application.ports.ProjectServicePort;
import com.example.colaba.shared.webmvc.infrastructure.circuit.ProjectServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectServiceAdapter implements ProjectServicePort {
    private final ProjectServiceClientWrapper wrapper;

    @Override
    public boolean projectExists(Long id) {
        return wrapper.projectExists(id);
    }

    @Override
    public TagResponse getTagById(Long id) {
        return wrapper.getTagById(id);
    }

    @Override
    public List<TagResponse> getTagsByIds(List<Long> ids) {
        return wrapper.getTagsByIds(ids);
    }

    @Override
    public boolean hasAnyRole(Long projectId, Long userId) {
        return wrapper.hasAnyRole(projectId, userId);
    }

    @Override
    public boolean isAtLeastEditor(Long projectId, Long userId) {
        return wrapper.isAtLeastEditor(projectId, userId);
    }

    @Override
    public boolean isOwner(Long projectId, Long userId) {
        return wrapper.isOwner(projectId, userId);
    }

    @Override
    public String getUserProjectRole(Long projectId, Long userId) {
        return wrapper.getUserProjectRole(projectId, userId);
    }
}