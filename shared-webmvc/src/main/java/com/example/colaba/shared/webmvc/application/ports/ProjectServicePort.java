package com.example.colaba.shared.webmvc.application.ports;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;

import java.util.List;

public interface ProjectServicePort {
    boolean projectExists(Long projectId);

    TagResponse getTagById(Long tagId);

    List<TagResponse> getTagsByIds(List<Long> tagIds);

    boolean hasAnyRole(Long projectId, Long userId);

    boolean isAtLeastEditor(Long projectId, Long userId);

    boolean isOwner(Long projectId, Long userId);

    String getUserProjectRole(Long projectId, Long userId);
}