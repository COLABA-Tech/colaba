package com.example.colaba.project.service;

import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.dto.tag.CreateTagRequest;
import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.shared.dto.tag.UpdateTagRequest;
import com.example.colaba.shared.entity.Tag;
import com.example.colaba.shared.entity.task.Task;
import com.example.colaba.shared.exception.tag.DuplicateTagException;
import com.example.colaba.shared.exception.tag.TagNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ProjectService projectService;
    //    private final TaskService taskService;  // TODO
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
                .flatMap(project -> Mono.fromCallable(() ->
                                tagMapper.toTagResponsePage(tagRepository.findByProject(project, pageable)))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<List<TagResponse>> getTagsByTask(Long taskId) {
        return Mono.fromCallable(() -> tagRepository.findByTaskId(taskId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(tags -> tags.stream()
                        .map(tagMapper::toTagResponse)
                        .toList());
    }

    public Mono<TagResponse> createTag(CreateTagRequest request) {
        return projectService.getProjectEntityById(request.projectId())
                .flatMap(project -> Mono.fromCallable(() -> {
                    if (tagRepository.findByProjectIdAndNameIgnoreCase(project.getId(), request.name()).isPresent()) {
                        throw new DuplicateTagException(request.name(), project.getId());
                    }

                    Tag tag = Tag.builder()
                            .name(request.name())
                            .project(project)
                            .build();

                    Tag savedTag = tagRepository.save(tag);
                    return tagMapper.toTagResponse(savedTag);
                })).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<TagResponse> updateTag(Long id, UpdateTagRequest request) {
        return Mono.fromCallable(() -> tagRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalTag -> {
                    if (optionalTag.isEmpty()) {
                        return Mono.error(new TagNotFoundException(id));
                    }
                    Tag tag = optionalTag.get();

                    boolean hasChanges = false;
                    if (request.name() != null && !request.name().equals(tag.getName())) {
                        // Проверка на дубликат имени в рамках проекта
                        if (tagRepository.findByProjectIdAndNameIgnoreCase(tag.getProject().getId(), request.name()).isPresent()) {
                            return Mono.error(new DuplicateTagException(request.name(), tag.getProject().getId()));
                        }
                        tag.setName(request.name());
                        hasChanges = true;
                    }

                    if (hasChanges) {
                        return Mono.fromCallable(() -> tagRepository.save(tag))
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(tagMapper::toTagResponse);
                    } else {
                        return Mono.just(tagMapper.toTagResponse(tag));
                    }
                });
    }

    public Mono<Void> deleteTag(Long id) {
        return Mono.fromCallable(() -> tagRepository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new TagNotFoundException(id));
                    }
                    return Mono.fromRunnable(() -> tagRepository.deleteById(id))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .then();
    }

    public Mono<Void> assignTagToTask(Long taskId, Long tagId) {
        return Mono.zip(
                // TODO
                // taskService.getTaskEntityById(taskId)
                Mono.fromCallable(() -> new Task()).subscribeOn(Schedulers.boundedElastic()),
                Mono.fromCallable(() -> tagRepository.findById(tagId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalTag -> {
                            if (optionalTag.isEmpty()) {
                                return Mono.error(new TagNotFoundException(tagId));
                            }
                            return Mono.just(optionalTag.get());
                        })
        ).flatMap(tuple -> {
            Task task = tuple.getT1();
            Tag tag = tuple.getT2();

            if (!tag.getProject().getId().equals(task.getProject().getId())) {
                return Mono.error(new IllegalArgumentException("Tag does not belong to task's project"));
            }

            return Mono.fromRunnable(() -> {
                boolean added = task.getTags().add(tag);
                if (added) {
                    tag.getTasks().add(task);
                    // TODO
                    // taskService.saveTask(task);
                }
            }).subscribeOn(Schedulers.boundedElastic());
        }).then();
    }

    public Mono<Void> removeTagFromTask(Long taskId, Long tagId) {
        return Mono.zip(
                // TODO
                // taskService.getTaskEntityById(taskId)
                Mono.fromCallable(() -> new Task()).subscribeOn(Schedulers.boundedElastic()),
                Mono.fromCallable(() -> tagRepository.findById(tagId))
                        .subscribeOn(Schedulers.boundedElastic())
                        .flatMap(optionalTag -> {
                            if (optionalTag.isEmpty()) {
                                return Mono.error(new TagNotFoundException(tagId));
                            }
                            return Mono.just(optionalTag.get());
                        })
        ).flatMap(tuple -> {
            Task task = tuple.getT1();
            Tag tag = tuple.getT2();

            return Mono.fromRunnable(() -> {
                // TODO
                // task.getTags().remove(tag);
                // taskService.saveTask(task);
            }).subscribeOn(Schedulers.boundedElastic());
        }).then();
    }
}
