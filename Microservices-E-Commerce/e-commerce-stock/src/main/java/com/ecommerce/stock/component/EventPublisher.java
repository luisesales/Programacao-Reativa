package com.ecommerce.stock.component;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;

import com.ecommerce.stock.event.DomainEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Mono;

@Component
public class EventPublisher {

    private static final Logger logger =
        LoggerFactory.getLogger(EventPublisher.class);

    private final Sinks.Many<DomainEvent> sink =
        Sinks.many().multicast().onBackpressureBuffer();

    public void publish(DomainEvent event) {
        Sinks.EmitResult result = sink.tryEmitNext(event);
        logger.info(
            "Publishing DomainEvent sagaId={}, result={}",
            event.sagaId(),
            result
        );
    }

    @Bean
    public Supplier<Flux<DomainEvent>> eventEmitter() {
        return sink::asFlux;
    }
}
