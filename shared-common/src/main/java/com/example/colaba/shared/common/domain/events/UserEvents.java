package com.example.colaba.shared.common.domain.events;

public class UserEvents {
    public record UserDeletedEvent(Long userId) {
    }
}
