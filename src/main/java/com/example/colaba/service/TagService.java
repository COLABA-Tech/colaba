package com.example.colaba.service;

import com.example.colaba.dto.tag.CreateTagRequest;
import com.example.colaba.dto.tag.TagResponse;
import com.example.colaba.dto.tag.UpdateTagRequest;
import com.example.colaba.entity.Project;
import com.example.colaba.entity.Tag;
import com.example.colaba.entity.task.Task;
import com.example.colaba.exception.tag.DuplicateTagException;
import com.example.colaba.exception.tag.TagNotFoundException;
import com.example.colaba.mapper.TagMapper;
import com.example.colaba.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final TagMapper tagMapper;

    public Page<TagResponse> getAllTags(Pageable pageable) {
        return tagMapper.toTagResponsePage(tagRepository.findAll(pageable));
    }

    public TagResponse getTagById(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));
        return tagMapper.toTagResponse(tag);
    }

    public Page<TagResponse> getTagsByProject(Long projectId, Pageable pageable) {
        Project project = projectService.getProjectEntityById(projectId);
        return tagMapper.toTagResponsePage(tagRepository.findByProject(project, pageable));
    }

    public List<TagResponse> getTagsByTask(Long taskId) {
        return tagRepository.findByTaskId(taskId).stream()
                .map(tagMapper::toTagResponse)
                .toList();
    }

    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        Project project = projectService.getProjectEntityById(request.projectId());
        if (tagRepository.findByProjectIdAndNameIgnoreCase(project.getId(), request.name()).isPresent()) {
            throw new DuplicateTagException(request.name(), project.getId());
        }

        Tag tag = Tag.builder()
                .name(request.name())
                .project(project)
                .build();

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toTagResponse(savedTag);
    }

    @Transactional
    public TagResponse updateTag(Long id, UpdateTagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        boolean hasChanges = false;
        if (request.name() != null && !request.name().equals(tag.getName())) {
            if (tagRepository.findByProjectIdAndNameIgnoreCase(tag.getProject().getId(), request.name()).isPresent()) {
                throw new DuplicateTagException(request.name(), tag.getProject().getId());
            }
            tag.setName(request.name());
            hasChanges = true;
        }

        Tag updatedTag = hasChanges ? tagRepository.save(tag) : tag;
        return tagMapper.toTagResponse(updatedTag);
    }

    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new TagNotFoundException(id);
        }
        tagRepository.deleteById(id);
    }

    @Transactional
    public void assignTagToTask(Long taskId, Long tagId) {
        Task task = taskService.getTaskEntityById(taskId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));
        if (!tag.getProject().getId().equals(task.getProject().getId())) {
            throw new IllegalArgumentException("Tag does not belong to task's project");
        }
        boolean added = task.getTags().add(tag);
        if (added) {
            tag.getTasks().add(task);
            taskService.saveTask(task);
        }
    }

    @Transactional
    public void removeTagFromTask(Long taskId, Long tagId) {
        Task task = taskService.getTaskEntityById(taskId);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new TagNotFoundException(tagId));
        task.getTags().remove(tag);
        taskService.saveTask(task);
    }
}
