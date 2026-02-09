package com.example.colaba.shared.webmvc.application.ports;

import com.example.colaba.shared.common.application.dto.user.UserAuthDto;

public interface UserServicePort {
    boolean userExists(Long userId);

    boolean isAdmin(Long userId);

    UserAuthDto findForAuthByUsername(String username);

    UserAuthDto findForAuthByEmail(String email);

    UserAuthDto createUser(UserAuthDto userAuthDto);
}