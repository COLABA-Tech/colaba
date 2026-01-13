package com.example.colaba.user.dto.user;

import com.example.colaba.shared.common.dto.user.UserResponse;

import java.util.List;

public record UserScrollResponse(
        List<UserResponse> users,
        String nextCursor,
        boolean hasMore
) {
}
