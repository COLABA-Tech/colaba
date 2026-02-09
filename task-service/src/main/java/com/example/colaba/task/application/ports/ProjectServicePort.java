package com.example.colaba.task.application.ports;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;

import java.util.List;

public interface ProjectServicePort {
    boolean projectExists(Long projectId);

    TagResponse getTagById(Long tagId);

    List<TagResponse> getTagsByIds(List<Long> tagIds);
}