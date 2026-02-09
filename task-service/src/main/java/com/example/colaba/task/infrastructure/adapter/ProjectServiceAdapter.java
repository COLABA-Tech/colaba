package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.tag.TagResponse;
import com.example.colaba.task.application.ports.ProjectServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectServiceAdapter implements ProjectServicePort {
    private final com.example.colaba.shared.webmvc.infrastructure.circuit.ProjectServiceClientWrapper wrapper;

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
}
