package com.example.colaba.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserScrollResponse {
    private List<UserResponse> users;
    private String nextCursor;
    private boolean hasMore;
}
