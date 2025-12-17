package com.ecommerce.stock.component;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.event.*;

import reactor.core.publisher.Mono;
@Component
public class EventPublisher {

    private static final Logger logger =
        LoggerFactory.getLogger(EventPublisher.class);

    private final StreamBridge streamBridge;

    public EventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    private static final Map<String, String> ROUTES = Map.of(
        "stockReserved", "stockReserved-out-0",
        "stockRejected", "stockRejected-out-0",
        "stockIncreaseApproved", "stockIncreaseApproved-out-0",
        "stockIncreaseRejected", "stockIncreaseRejected-out-0"
    );

   public Mono<Void> publish(DomainEvent event) {
        String binding = ROUTES.get(event.eventType());

        if (binding == null) {
            return Mono.error(
                new IllegalArgumentException(
                    "No binding configured for eventType=" + event.eventType()
                )
            );
        }

        boolean sent = streamBridge.send(binding, event);

        logger.info(
            "Outbox publish eventType={} sagaId={} binding={} sent={}",
            event.eventType(),
            event.sagaId(),
            binding,
            sent
        );

        return sent
            ? Mono.empty()
            : Mono.error(new IllegalStateException("Failed to send event"));
    }
}