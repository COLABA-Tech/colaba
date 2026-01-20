package com.example.colaba.project.service;

import com.example.colaba.project.dto.tag.CreateTagRequest;
import com.example.colaba.project.dto.tag.UpdateTagRequest;
import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import com.example.colaba.shared.common.exception.tag.DuplicateTagException;
import com.example.colaba.shared.common.exception.tag.TagNotFoundException;
import com.example.colaba.shared.webflux.circuit.TaskServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ProjectService projectService;
    private final TaskServiceClientWrapper taskServiceClient;
    private final TagMapper tagMapper;
    private final TransactionTemplate transactionTemplate;

    public Mono<Page<TagResponse>> getAllTags(Pageable pageable) {
        return Mono.fromCallable(() ->
                tagMapper.toTagResponsePage(tagRepository.findAll(pageable))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TagResponse> getTagById(Long id) {
        return getTagEntityById(id).map(tagMapper::toTagResponse);
    }

    public Mono<TagJpa> getTagEntityById(Long id) {
        return Mono.fromCallable(() -> tagRepository.findById(id)
                        .orElseThrow(() -> new TagNotFoundException(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Page<TagResponse>> getTagsByProject(Long projectId, Pageable pageable) {
        return projectService.getProjectEntityById(projectId)
                .then(Mono.fromCallable(() ->
                        tagMapper.toTagResponsePage(tagRepository.findByProjectId(projectId, pageable))
                ).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<TagResponse> createTag(CreateTagRequest request) {
        return projectService.getProjectEntityById(request.projectId())
                .flatMap(project -> Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
                    if (tagRepository.findByProjectIdAndNameIgnoreCase(project.getId(), request.name()).isPresent()) {
                        throw new DuplicateTagException(request.name(), project.getId());
                    }
                    TagJpa tag = TagJpa.builder()
                            .name(request.name())
                            .projectId(project.getId())
                            .build();
                    TagJpa savedTag = tagRepository.save(tag);
                    return tagMapper.toTagResponse(savedTag);
                })).subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<TagResponse> updateTag(Long id, UpdateTagRequest request) {
        return Mono.fromCallable(() -> transactionTemplate.execute(_ -> {
            TagJpa tag = tagRepository.findById(id)
                    .orElseThrow(() -> new TagNotFoundException(id));

            boolean hasChanges = false;
            if (request.name() != null && !request.name().isBlank() && !request.name().equals(tag.getName())) {
                if (tagRepository.findByProjectIdAndNameIgnoreCase(tag.getProjectId(), request.name()).isPresent()) {
                    throw new DuplicateTagException(request.name(), tag.getProjectId());
                }
                tag.setName(request.name());
                hasChanges = true;
            }

            TagJpa saved = hasChanges ? tagRepository.save(tag) : tag;
            return tagMapper.toTagResponse(saved);
        })).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> deleteTag(Long id) {
        return Mono.fromCallable(() -> {
                    if (!tagRepository.existsById(id)) {
                        throw new TagNotFoundException(id);
                    }
                    return id;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(_ -> taskServiceClient.deleteTaskTagsByTagId(id))
                .then(Mono.fromRunnable(() -> transactionTemplate.executeWithoutResult(_ ->
                        tagRepository.deleteById(id)
                )).subscribeOn(Schedulers.boundedElastic()))
                .then();
    }
}