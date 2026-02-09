package com.example.colaba.shared.webmvc.application.ports;

import com.example.colaba.shared.common.application.events.DomainEvent;

public interface EventPublisher {
    void publish(DomainEvent<?> event);
}