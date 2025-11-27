package com.example.colaba.user.mapper;

import com.example.colaba.shared.dto.user.UserResponse;
import com.example.colaba.shared.entity.User;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    default Page<UserResponse> toUserResponsePage(Page<User> users) {
        return users.map(this::toUserResponse);
    }
}