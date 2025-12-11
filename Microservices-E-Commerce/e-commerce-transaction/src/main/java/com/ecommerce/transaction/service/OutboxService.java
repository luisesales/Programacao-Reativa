package com.ecommerce.transaction.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ecommerce.transaction.model.outbox.OutboxEvent;
import com.ecommerce.transaction.model.outbox.OutboxEventContext;
import com.ecommerce.transaction.repository.OutboxContextRepository;
import com.ecommerce.transaction.repository.OutboxRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final OutboxContextRepository outboxContextRepository;

    public OutboxService(OutboxRepository outboxRepository,
                         OutboxContextRepository outboxContextRepository) {
        this.outboxRepository = outboxRepository;
        this.outboxContextRepository = outboxContextRepository;
    }

    public Mono<OutboxEvent> saveOutbox(        
        UUID sagaId,
        UUID orderId,
        UUID transactionId,
        String errorMessage,
        String eventType

    ) {
        OutboxEvent outbox = new OutboxEvent(
                "TRANSACTION",
                eventType,
                false,
                LocalDateTime.now()
        );
        return outboxRepository.save(outbox)
              .thenMany(
                Flux.fromIterable(buildUpdatedContext(
                    outbox.getId(), sagaId, orderId, transactionId, errorMessage
                )).flatMap(outboxContextRepository::save)
            ).then(Mono.just(outbox));     
    }

    public Mono<Void> updateOutbox(
        UUID outboxId,
        UUID sagaId,
        UUID orderId,
        UUID transactionId,
        String errorMessage,
        String newEventType
    ) {
        return outboxRepository.findById(outboxId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException(
                "OutboxEvent not found for id: " + outboxId)))
            .flatMap(existing -> {
                
                existing.setEventType(newEventType);
                existing.setCreatedAt(LocalDateTime.now());
                existing.setPublished(false);  

                return outboxRepository.save(existing);
            })
            .then(
                outboxContextRepository.deleteByOutboxEventId(outboxId)
            )
            .thenMany(
                Flux.fromIterable(buildUpdatedContext(
                    outboxId, sagaId, orderId, transactionId, errorMessage
                )).flatMap(outboxContextRepository::save)
            )
            .then();
    }


    private List<OutboxEventContext> buildUpdatedContext(
            UUID outboxId,
            UUID sagaId,
            UUID orderId,
            UUID transactionId,
            String errorMessage
    ) {
        List<OutboxEventContext> ctx = new ArrayList<>();
        ctx.add(new OutboxEventContext(outboxId, "sagaId", sagaId.toString(), orderId));
        ctx.add(new OutboxEventContext(outboxId, "orderId", orderId.toString(), orderId));
        ctx.add(new OutboxEventContext(outboxId, "transactionId", transactionId.toString(), orderId));        
        if (errorMessage != null) {
            ctx.add(new OutboxEventContext(outboxId, "error", errorMessage, orderId));
        }
        return ctx;
    }

    public Mono<OutboxEvent> findOutboxEventById(UUID outboxId) {
        return outboxRepository.findById(outboxId);
    }

    public Flux<OutboxEventContext> findByOrderId(UUID orderId) {
        return outboxContextRepository.findByOrderId(orderId);
    }

    
}
