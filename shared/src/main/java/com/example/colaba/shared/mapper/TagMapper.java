package com.example.colaba.shared.mapper;

import com.example.colaba.shared.dto.tag.TagResponse;
import com.example.colaba.shared.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse toTagResponse(Tag tag);

    default Page<TagResponse> toTagResponsePage(Page<Tag> tags) {
        return tags.map(this::toTagResponse);
    }
}
