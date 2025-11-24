package com.example.colaba.project.mapper;

import com.example.colaba.project.entity.Tag;
import com.example.colaba.shared.dto.tag.TagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TagMapper {
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    TagResponse toTagResponse(Tag tag);

    default Page<TagResponse> toTagResponsePage(Page<Tag> tags) {
        return tags.map(this::toTagResponse);
    }
}
