package com.example.colaba.user.mapper;

import com.example.colaba.user.dto.user.UserResponse;
import com.example.colaba.user.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);
}