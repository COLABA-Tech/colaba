package com.example.colaba.project.controller;

import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/tags/internal")
@RequiredArgsConstructor
@Tag(name = "Tags Internal", description = "Internal Tags API")
public class TagInternalController {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @GetMapping("/{id}")
    public Mono<TagResponse> getTagById(@PathVariable Long id) {
        return Mono.fromCallable(() ->
                tagRepository.findById(id)
                        .map(tagMapper::toTagResponse)
                        .orElse(null)
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/batch")
    public Mono<List<TagResponse>> getTagsByIds(@RequestBody List<Long> tagIds) {
        return Mono.fromCallable(() ->
                tagRepository.findAllById(tagIds).stream()
                        .map(tagMapper::toTagResponse)
                        .toList()
        );
    }

    @GetMapping("/{id}/exists")
    public Mono<Boolean> tagExists(@PathVariable Long id) {
        return Mono.fromCallable(() ->
                tagRepository.existsById(id)
        ).subscribeOn(Schedulers.boundedElastic());
    }
}