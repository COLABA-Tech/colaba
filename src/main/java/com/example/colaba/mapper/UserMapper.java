package com.example.colaba.mapper;

import com.example.colaba.dto.user.UserResponse;
import com.example.colaba.entity.User;
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