package com.ecommerce.stock.component;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.event.DomainEvent;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.outbox.OutboxEvent;
import com.ecommerce.stock.model.outbox.OutboxEventContext;
import com.ecommerce.stock.repository.OutboxContextRepository;
import com.ecommerce.stock.repository.OutboxRepository;
import com.ecommerce.stock.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class OutboxProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);


    private final OutboxRepository outboxRepository;
    private final OutboxContextRepository outboxContextRepository;
    private final EventPublisher eventPublisher;
    private final ProductRepository productRepository;


    public OutboxProcessor(OutboxRepository outboxRepository, EventPublisher eventPublisher, OutboxContextRepository outboxContextRepository, ProductRepository productRepository) {

        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.outboxContextRepository = outboxContextRepository;
        this.productRepository = productRepository;
        start();
    }


private void start() {
    Flux.defer(() -> outboxRepository.findPendingEvents())
        .flatMap(outboxEvent ->
            outboxContextRepository.findByOutboxEventId(outboxEvent.getId())
                .flatMap(context ->
                    productRepository.findById(context.getProductId())
                        .map(stock ->
                            toDomainEvent(outboxEvent, context, stock)
                        )
                )
                .flatMap(eventPublisher::publish)
                .flatMap(result -> outboxRepository.markAsSent(outboxEvent.getId()))
                .onErrorResume(ex -> handleFailure(outboxEvent, ex))
        )
        .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(5)))
        .subscribe();
}

    private DomainEvent toDomainEvent(OutboxEvent event, OutboxEventContext context, Order tx) {        
        return context.toDomainEvent(event.getEventType(),tx);
    }

    private Mono<Integer> handleFailure(OutboxEvent event, Throwable ex) {
        if (event.getRetryCount() >= 10) {
            return outboxRepository.markAsFailed(event.getId());
        }
        return outboxRepository.incrementRetry(event.getId(), ex.getMessage());
    }
}

