package com.ecommerce.order.component;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.ecommerce.order.event.DomainEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class EventPublisher {

    private final Sinks.Many<DomainEvent> sink = Sinks.many().unicast().onBackpressureBuffer();

    public void publish(DomainEvent event) {
        sink.tryEmitNext(event);
    }

    public Supplier<Flux<DomainEvent>> emitter() {
        return sink::asFlux;
    }
}
