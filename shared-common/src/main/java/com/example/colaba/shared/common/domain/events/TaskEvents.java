package com.example.colaba.shared.common.domain.events;

public class TaskEvents {
    public record TaskDeletedEvent(Long taskId) {
    }
}
