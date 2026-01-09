package com.example.colaba.project.mapper;

import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.shared.dto.tag.TagResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse toTagResponse(TagJpa tag);

    default Page<TagResponse> toTagResponsePage(Page<TagJpa> tags) {
        return tags.map(this::toTagResponse);
    }
}
