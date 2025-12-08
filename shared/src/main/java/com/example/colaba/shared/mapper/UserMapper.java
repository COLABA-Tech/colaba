package com.example.colaba.shared.mapper;

import com.example.colaba.shared.dto.user.UserResponse;
import com.example.colaba.shared.entity.User;
import com.example.colaba.shared.entity.UserJpa;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.HashSet;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    default Page<UserResponse> toUserResponsePage(Page<User> users) {
        return users.map(this::toUserResponse);
    }

    default UserJpa toUserJpa(User user) {
        if (user == null) {
            return null;
        }
        return UserJpa.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .memberships(new HashSet<>())
                .build();
    }
}