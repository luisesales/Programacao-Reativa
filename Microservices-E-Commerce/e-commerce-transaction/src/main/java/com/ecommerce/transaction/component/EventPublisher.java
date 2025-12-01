package com.ecommerce.transaction.component;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.ecommerce.transaction.event.DomainEvent;
import com.ecommerce.transaction.event.TransactionApproved;
import com.ecommerce.transaction.event.TransactionRefundApproved;
import com.ecommerce.transaction.event.TransactionRefundRejected;
import com.ecommerce.transaction.event.TransactionRejected;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

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

    private Class<? extends DomainEvent> resolveEventClass(String type) {
    return switch (type) {
        case "TransactionApproved" -> TransactionApproved.class;
        case "TransactionRejected" -> TransactionRejected.class;
        case "TransactionRefundApproved" -> TransactionRefundApproved.class;
        case "TransactionRefundRejected" -> TransactionRefundRejected.class;
        default -> throw new IllegalArgumentException("Unknown event type: " + type);
    };
}
}
