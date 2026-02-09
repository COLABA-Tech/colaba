package com.example.colaba.task.infrastructure.adapter;

import com.example.colaba.task.application.ports.UserServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserServiceAdapter implements UserServicePort {
    private final com.example.colaba.shared.webmvc.infrastructure.circuit.UserServiceClientWrapper wrapper;

    @Override
    public boolean userExists(Long id) {
        return wrapper.userExists(id);
    }

    @Override
    public boolean isAdmin(Long id) {
        return wrapper.isAdmin(id);
    }
}
