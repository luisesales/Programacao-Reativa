package com.ecommerce.stock.component;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.ecommerce.stock.event.DomainEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Mono;

@Component
public class EventPublisher {

    private final Sinks.Many<DomainEvent> sink = Sinks.many().unicast().onBackpressureBuffer();

    public Mono<Void> publish(DomainEvent event) {
        sink.tryEmitNext(event);
        return Mono.empty();
    }

    public Supplier<Flux<DomainEvent>> emitter() {
        return sink::asFlux;
    }
}
