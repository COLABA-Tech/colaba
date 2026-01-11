package com.example.colaba.project.controller;

import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.project.repository.TagRepository;
import com.example.colaba.shared.dto.tag.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tags/internal")
@RequiredArgsConstructor
public class TagInternalController {
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @GetMapping("/{id}")
    TagResponse getTagById(@PathVariable Long id) {
        return tagRepository.findById(id).map(tagMapper::toTagResponse).orElse(null);
    }

    @PostMapping("/batch")
    List<TagResponse> getTagsByIds(@RequestBody List<Long> tagIds) {
        return tagRepository.findAllById(tagIds).stream().map(tagMapper::toTagResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}/exists")
    boolean tagExists(@PathVariable Long id) {
        return tagRepository.existsById(id);
    }
}
