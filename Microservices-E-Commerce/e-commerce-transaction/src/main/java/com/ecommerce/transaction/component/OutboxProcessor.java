package com.ecommerce.transaction.component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ecommerce.transaction.event.DomainEvent;
import com.ecommerce.transaction.event.TransactionApproved;
import com.ecommerce.transaction.event.TransactionRejected;
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


private void start() {
    Flux.defer(() -> outboxRepository.findPendingEvents())
        .flatMap(outboxEvent ->
            outboxContextRepository.findByOutboxEventId(outboxEvent.getId())
                .collectList()
                .flatMap(context ->
                transactionRepository.findById(context.get(0).getTransactionId())
                    .map(transaction ->
                        processOutboxEvent(outboxEvent, context, transaction)
                    )
                 )                
        )
        .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(5)))
        .subscribe();
}

    private Mono<Void> processOutboxEvent(OutboxEvent outboxEvent, List<OutboxEventContext> contexts, Transaction transaction) {
                
        UUID sagaId    = getUUID(contexts, "sagaId");        
        String error   = getString(contexts, "error");

        return Mono.just(toDomainEvent(outboxEvent.getEventType(), transaction, sagaId, error))
            .flatMap(eventPublisher::publish)         
            .then(outboxRepository.markAsSent(outboxEvent.getId()))            
            .onErrorResume(ex -> handleFailure(outboxEvent, ex))
            .then();  
    }


    private Mono<Integer> handleFailure(OutboxEvent event, Throwable ex) {
        if (event.getRetryCount() >= 10) {
            return outboxRepository.markAsFailed(event.getId());
        }
        return outboxRepository.incrementRetry(event.getId(), ex.getMessage());
    }


    public DomainEvent toDomainEvent(String eventType, Transaction tx, UUID sagaId, String error) {
        return switch (eventType) {

            case "TransactionApproved" -> new TransactionApproved(
                sagaId,
                tx.getOrderId(),
                tx.getId()
            );

            case "TransactionRejected" -> new TransactionRejected(
                sagaId,
                tx.getId(),
                error        
            );

            case "TransactionRefundRequested" -> new TransactionRefundRequested(
                sagaId,
                tx.getOrderId(),
                tx.getName(),
                tx.getTotalPrice(),                     
            );

            case "TransactionRefundApproved" -> new TransactionRefundApproved(
                sagaId,
                tx.getOrderId(),
                tx.getId()
            );

            case "TransactionRefundRejected" -> new TransactionRefundRejected(
                sagaId,
                tx.getOrderId(),                
                error
            );
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

     private UUID getUUID(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(ctx -> UUID.fromString(ctx.getFieldValue()))
            .orElse(null);
    }
    private Integer getInt(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(ctx -> Integer.valueOf(ctx.getFieldValue()))
            .orElse(null);
    }
    private String getString(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(OutboxEventContext::getFieldValue)
            .orElse(null);
    }
}

