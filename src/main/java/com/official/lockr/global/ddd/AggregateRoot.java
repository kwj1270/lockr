package com.official.lockr.global.ddd;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

    private final List<DomainEvent> events;

    public AggregateRoot() {
        this(new ArrayList<>());
    }

    public AggregateRoot(final List<DomainEvent> events) {
        this.events = events;
    }

    protected void addEvent(DomainEvent event) {
        events.add(event);
    }

    public void publish(DomainEventPublisher publisher) {
        events.forEach(publisher::publish);
        events.clear();
    }
}

