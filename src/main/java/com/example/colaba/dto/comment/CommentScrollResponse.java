package com.example.colaba.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CommentScrollResponse(
        @JsonProperty("comments")
        List<CommentResponse> comments,

        @JsonProperty("nextCursor")
        String nextCursor,

        @JsonProperty("hasMore")
        boolean hasMore
) {
}