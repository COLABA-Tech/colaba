package com.example.colaba.shared.webmvc.infrastructure.adapter;

import com.example.colaba.shared.common.application.dto.user.UserAuthDto;
import com.example.colaba.shared.webmvc.application.ports.UserServicePort;
import com.example.colaba.shared.webmvc.infrastructure.circuit.UserServiceClientWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceAdapter implements UserServicePort {
    private final UserServiceClientWrapper wrapper;

    @Override
    public boolean userExists(Long id) {
        return wrapper.userExists(id);
    }

    @Override
    public boolean isAdmin(Long id) {
        return wrapper.isAdmin(id);
    }

    @Override
    public UserAuthDto findForAuthByUsername(String username) {
        return wrapper.findForAuthByUsername(username);
    }

    @Override
    public UserAuthDto findForAuthByEmail(String email) {
        return wrapper.findForAuthByEmail(email);
    }

    @Override
    public UserAuthDto createUser(UserAuthDto userAuthDto) {
        return wrapper.createUser(userAuthDto);
    }
}