package com.official.lockr.global.ddd;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
