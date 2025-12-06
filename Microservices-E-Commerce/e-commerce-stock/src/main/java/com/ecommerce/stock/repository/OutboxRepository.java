package com.ecommerce.stock.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.stock.model.outbox.OutboxEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface OutboxRepository extends R2dbcRepository<OutboxEvent, UUID> {
    @Modifying
    @Query("""
        SELECT * FROM outbox_event
        WHERE published = FALSE
        ORDER BY created_at
        LIMIT 50
    """)
    Flux<OutboxEvent> findTop50ByPublishedFalseOrderByCreatedAtAsc();
    @Query("""
        SELECT * FROM outbox_event
        WHERE published = FALSE
        ORDER BY created_at
        LIMIT 100
    """)
    Flux<OutboxEvent> findPendingEvents();


    @Modifying
    @Query("""
        UPDATE outbox_event
        SET published = TRUE,
            published_at = NOW(),
            last_attempt_at = NOW()
        WHERE id = :eventId
    """)
    Mono<Integer> markAsSent(UUID eventId);

    @Modifying
    @Query("""
        UPDATE outbox_event
        SET published = FALSE,
            failed = TRUE,
            last_attempt_at = NOW()
        WHERE id = :eventId
    """)
    Mono<Integer> markAsFailed(UUID eventId);


    @Modifying
    @Query("""
        UPDATE outbox_event
        SET retry_count = retry_count + 1,
            last_attempt_at = NOW(),
            last_error = :errorMessage
        WHERE id = :eventId
    """)
    Mono<Integer> incrementRetry(UUID eventId, String errorMessage);
}
