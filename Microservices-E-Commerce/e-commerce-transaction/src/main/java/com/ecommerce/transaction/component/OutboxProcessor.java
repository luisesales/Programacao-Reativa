package com.ecommerce.transaction.component;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ecommerce.transaction.event.DomainEvent;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.outbox.OutboxEvent;
import com.ecommerce.transaction.model.outbox.OutboxEventContext;
import com.ecommerce.transaction.repository.OutboxContextRepository;
import com.ecommerce.transaction.repository.OutboxRepository;
import com.ecommerce.transaction.repository.TransactionRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class OutboxProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);


    private final OutboxRepository outboxRepository;
    private final OutboxContextRepository outboxContextRepository;
    private final EventPublisher eventPublisher;
    private final TransactionRepository transactionRepository;


    public OutboxProcessor(OutboxRepository outboxRepository, EventPublisher eventPublisher, OutboxContextRepository outboxContextRepository, TransactionRepository transactionRepository) {

        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.outboxContextRepository = outboxContextRepository;
        this.transactionRepository = transactionRepository;
        start();
    }

    // @PostConstruct
    // public void startOutboxProcessor() {
    //     Flux.defer(() -> outboxRepository.findPendingEvents())
    //     .flatMap(event ->
    //         outboxContextRepository.findByOutboxEventId(event.getId())
    //             .flatMap(payload -> {                    
    //                 DomainEvent domainEvent = objectMapper.readValue(
    //                     payload.getPayloadJson(),resolveEventClass(event.getEventType())
    //                 );
    //                 return eventPublisher.publish(domainEvent);
    //             })
    //             .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
    //             .then(outboxRepository.markAsSent(event.getId()))
    //             .onErrorResume(ex -> {
    //                 logger.warn("Failed to publish event {} after retries", event.getId());

    //                 if (event.getRetryCount() >= 10) {
    //                     return outboxRepository.markAsFailed(event.getId());
    //                 }
    //                 return outboxRepository.incrementRetry(event.getId(), ex.getMessage());
    //             })
    //     , 5)
    //     .doOnError(ex -> logger.error("Unexpected failure in Outbox processing", ex))
    //     .doOnComplete(() -> logger.info("Outbox cycle completed"))
    //     .repeatWhen(longFlux -> longFlux.delayElements(Duration.ofSeconds(5)))
    //     .subscribe();
    // }

private void start() {
    Flux.defer(() -> outboxRepository.findPendingEvents())
        .flatMap(outboxEvent ->
            outboxContextRepository.findByOutboxEventId(outboxEvent.getId())
                .flatMap(context ->
                    transactionRepository.findById(context.getTransactionId())
                        .map(transaction ->
                            toDomainEvent(outboxEvent, context, transaction)
                        )
                )
                .flatMap(eventPublisher::publish)
                .flatMap(result -> outboxRepository.markAsSent(outboxEvent.getId()))
                .onErrorResume(ex -> handleFailure(outboxEvent, ex))
        )
        .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(5)))
        .subscribe();
}

    private DomainEvent toDomainEvent(OutboxEvent event, OutboxEventContext context, Transaction tx) {        
        return context.toDomainEvent(event.getEventType(),tx);
    }

    private Mono<Integer> handleFailure(OutboxEvent event, Throwable ex) {
        if (event.getRetryCount() >= 10) {
            return outboxRepository.markAsFailed(event.getId());
        }
        return outboxRepository.incrementRetry(event.getId(), ex.getMessage());
    }
}

