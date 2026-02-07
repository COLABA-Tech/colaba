package com.example.colaba.shared.common.rabbit;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class DomainEvent<T> {
    private String eventType;
    private T payload;
    private Instant timestamp = Instant.now();
    private String sourceService;

    public DomainEvent(String eventType, T payload, String sourceService) {
        this.eventType = eventType;
        this.payload = payload;
        this.sourceService = sourceService;
    }
}