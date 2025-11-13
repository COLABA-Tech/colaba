package com.example.colaba.mapper;

import com.example.colaba.dto.tag.TagResponse;
import com.example.colaba.entity.Tag;
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
