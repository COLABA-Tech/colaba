package com.example.colaba.project.service;

import com.example.colaba.project.client.TaskServiceClient;
import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.shared.exception.tag.DuplicateTagException;
import com.example.colaba.shared.exception.tag.TagNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ProjectService projectService;
    private final TaskServiceClient taskServiceClient;
    private final TagMapper tagMapper;

    public Mono<Page<TagResponse>> getAllTags(Pageable pageable) {
        return Mono.fromCallable(() -> tagMapper.toTagResponsePage(tagRepository.findAll(pageable)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TagResponse> getTagById(Long id) {
        return Mono.fromCallable(() -> tagRepository.findById(id)
                        .map(tagMapper::toTagResponse)
                        .orElseThrow(() -> new TagNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<TagResponse>> getTagsByProject(Long projectId, Pageable pageable) {
        return projectService.getProjectEntityById(projectId)
                .flatMap(_ -> Mono.fromCallable(() ->
                                tagMapper.toTagResponsePage(tagRepository.findByProjectId(projectId, pageable)))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    @Transactional
    public Mono<TagResponse> createTag(CreateTagRequest request) {
        return projectService.getProjectEntityById(request.projectId())
                .flatMap(project -> Mono.fromCallable(() -> {
                    if (tagRepository.findByProjectIdAndNameIgnoreCase(project.getId(), request.name()).isPresent()) {
                        throw new DuplicateTagException(request.name(), project.getId());
                    }
                    TagJpa tag = TagJpa.builder()
                            .name(request.name())
                            .projectId(project.getId())
                            .build();
                    TagJpa savedTag = tagRepository.save(tag);
                    return tagMapper.toTagResponse(savedTag);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    @Transactional
    public Mono<TagResponse> updateTag(Long id, UpdateTagRequest request) {
        return Mono.fromCallable(() -> tagRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalTag -> {
                    if (optionalTag.isEmpty()) {
                        return Mono.error(new TagNotFoundException(id));
                    }
                    TagJpa tag = optionalTag.get();
                    return Mono.fromCallable(() -> {
                                boolean hasChanges = false;
                                if (request.name() != null && !request.name().equals(tag.getName())) {
                                    if (tagRepository.findByProjectIdAndNameIgnoreCase(
                                            tag.getProjectId(), request.name()).isPresent()) {
                                        throw new DuplicateTagException(request.name(), tag.getProjectId());
                                    }
                                    tag.setName(request.name());
                                    hasChanges = true;
                                }
                                return hasChanges ? tagRepository.save(tag) : tag;
                            }).subscribeOn(Schedulers.boundedElastic())
                            .map(tagMapper::toTagResponse);
                });
    }

    @Transactional
    public Mono<Void> deleteTag(Long id) {
        return Mono.fromCallable(() -> tagRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new TagNotFoundException(id));
                    }
                    return Mono.fromRunnable(() -> {
                                taskServiceClient.deleteTaskTagsByTagId(id);
                                tagRepository.deleteById(id);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .then();
    }
}
