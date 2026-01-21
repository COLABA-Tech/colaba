package com.example.colaba.project.unit;

import com.example.colaba.project.entity.TagJpa;
import com.example.colaba.project.mapper.TagMapper;
import com.example.colaba.shared.common.dto.tag.TagResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagMapperTest {

    private TagMapper tagMapper;

    @BeforeEach
    void setUp() {
        tagMapper = Mappers.getMapper(TagMapper.class);
    }

    @Test
    void toTagResponse_shouldMapSingleTag() {
        // Arrange
        TagJpa tagJpa = new TagJpa();

        // Act
        TagResponse response = tagMapper.toTagResponse(tagJpa);

        // Assert
        assertNotNull(response);
    }

    @Test
    void toTagResponsePage_shouldMapPageOfTags() {
        // Arrange
        TagJpa tag1 = new TagJpa();

        TagJpa tag2 = new TagJpa();

        List<TagJpa> content = Arrays.asList(tag1, tag2);
        Page<TagJpa> tagPage = new PageImpl<>(content, PageRequest.of(0, 2), 2);

        // Act
        Page<TagResponse> responsePage = tagMapper.toTagResponsePage(tagPage);

        // Assert
        assertNotNull(responsePage);
        assertEquals(2, responsePage.getContent().size());
        assertEquals(0, responsePage.getNumber());
        assertEquals(2, responsePage.getSize());
        assertEquals(2, responsePage.getTotalElements());
    }

    @Test
    void toTagResponsePage_shouldHandleEmptyPage() {
        // Arrange
        Page<TagJpa> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // Act
        Page<TagResponse> responsePage = tagMapper.toTagResponsePage(emptyPage);

        // Assert
        assertNotNull(responsePage);
        assertTrue(responsePage.getContent().isEmpty());
        assertEquals(0, responsePage.getTotalElements());
    }
}
