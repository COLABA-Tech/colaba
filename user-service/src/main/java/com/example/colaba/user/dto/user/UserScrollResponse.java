package com.example.colaba.user.dto.user;

import java.util.List;

public record UserScrollResponse(
        List<UserResponse> users,
        String nextCursor,
        boolean hasMore
) {
}
