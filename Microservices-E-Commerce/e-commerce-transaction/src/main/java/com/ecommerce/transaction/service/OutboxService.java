package com.ecommerce.transaction.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.transaction.model.outbox.OutboxEvent;
import com.ecommerce.transaction.model.outbox.OutboxEventContext;
import com.ecommerce.transaction.repository.OutboxContextRepository;
import com.ecommerce.transaction.repository.OutboxRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OutboxService {

    private static final Logger logger = LoggerFactory.getLogger(OutboxService.class);

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
        logger.info("Saving Outbox for event with sagaId {}, orderId {}, productId {} and quantity {}, eventType",sagaId,orderId,productId,quantity,eventType);
        OutboxEvent outbox = new OutboxEvent(
                "TRANSACTION",
                eventType,
                false,
                LocalDateTime.now()
        );
        return outboxRepository.save(outbox)
              .doOnSuccess(s -> logger.info("OutboxEvent saved succesfuly for sagaId {}",sagaId))
              .thenMany(
                Flux.fromIterable(buildUpdatedContext(
                    outbox.getId(), sagaId, orderId, transactionId, errorMessage
                )).flatMap(outboxContextRepository::save)
            ).then(Mono.just(outbox))
            .onErrorResume(e -> {
                logger.error("Internal Server Error on saving Outbox for sagaId {}", sagaId);
                return Mono.error(new SQLException("Internal Server Error : " + e));
            });     
    }

    public Mono<Void> updateOutbox(
        UUID outboxId,
        UUID sagaId,
        UUID orderId,
        UUID transactionId,
        String errorMessage,
        String newEventType
    ) {
        logger.info("Updating existing Outbox for event with sagaId {}, orderId {}, productId {} and quantity {}, eventType",sagaId,orderId,productId,quantity,newEventType);
        return outboxRepository.findById(outboxId)
            .switchIfEmpty(Mono.defer(() -> {
                logger.error("OutboxEvent not found for id : {}",outboxId);
                return Mono.error(new IllegalArgumentException(
                "OutboxEvent not found for id: " + outboxId));
            }))
            .flatMap(existing -> {
                logger.info("OutboxEvent found for id : {}",outboxId);
                existing.setEventType(newEventType);
                existing.setCreatedAt(LocalDateTime.now());
                existing.setPublished(false);  

                return outboxRepository.save(existing)
                    .onErrorResume(e -> {
                        logger.error("Internal Server Error on updating Outbox for sagaId {}", sagaId);
                        return Mono.error(new SQLException("Internal Server Error : " + e));
                    });
            })
            .then(
                outboxContextRepository.deleteByOutboxEventId(outboxId)
            )
            .thenMany(
                Flux.fromIterable(buildUpdatedContext(
                    outboxId, sagaId, orderId, transactionId, errorMessage
                )).flatMap(outboxContextRepository::save)
                .onErrorResume(e -> {
                    logger.error("Internal Server Error on saving OutboxContext for sagaId {} and outboxId {}", sagaId, outboxId);
                    return Mono.error(new SQLException("Internal Server Error : " + e));
                })
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
        logger.info("Updating outboxContext for sagaId {} on outboxId {} for productId {} and orderId {}",sagaId,outboxId,productId,orderId);
        List<OutboxEventContext> ctx = new ArrayList<>();
        ctx.add(new OutboxEventContext(outboxId, "sagaId", sagaId.toString(), orderId));
        ctx.add(new OutboxEventContext(outboxId, "orderId", orderId.toString(), orderId));
        ctx.add(new OutboxEventContext(outboxId, "transactionId", transactionId.toString(), orderId));        
        if (errorMessage != null) {
            logger.error("Error registered on outboxContext for outboxId {}, error: {}",outboxId,errorMessage);
            ctx.add(new OutboxEventContext(outboxId, "error", errorMessage, orderId));
        }
        return ctx;
    }

    public Mono<OutboxEvent> findOutboxEventById(UUID outboxId) {
        return outboxRepository.findById(outboxId)
            .onErrorResume(e -> {
                    logger.error("Internal Server Error on finding Outbox for outboxId {}",outboxId);
                    return Mono.error(new SQLException("Internal Server Error : " + e));
                });
    }

    // public Flux<OutboxEventContext> findByOrderId(UUID orderId) {
    //     return outboxContextRepository.findByOrderId(orderId);
    // }

    
}
