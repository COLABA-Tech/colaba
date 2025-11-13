package com.example.colaba.dto.user;

import java.util.List;

public record UserScrollResponse(
        List<UserResponse> users,
        String nextCursor,
        boolean hasMore
) {
}
