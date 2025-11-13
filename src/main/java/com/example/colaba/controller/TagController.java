package com.example.colaba.controller;

import com.example.colaba.dto.tag.CreateTagRequest;
import com.example.colaba.dto.tag.TagResponse;
import com.example.colaba.dto.tag.UpdateTagRequest;
import com.example.colaba.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController extends BaseController {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<Page<TagResponse>> getAllTags(Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TagResponse> tags = tagService.getAllTags(pageable);
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTagById(@PathVariable Long id) {
        TagResponse tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    // TODO: move to project controller
    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TagResponse>> getTagsByProject(
            @PathVariable Long projectId, Pageable pageable) {
        pageable = validatePageable(pageable);
        Page<TagResponse> tags = tagService.getTagsByProject(projectId, pageable);
        return ResponseEntity.ok(tags);
    }

    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse tag = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        TagResponse tag = tagService.updateTag(id, request);
        return ResponseEntity.ok(tag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
