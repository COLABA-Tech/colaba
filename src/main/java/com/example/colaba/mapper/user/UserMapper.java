package com.example.colaba.mapper.user;

import com.example.colaba.dto.user.UserResponse;
import com.example.colaba.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper
// @Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    Page<UserResponse> toUserResponsePage(Page<User> users);
}