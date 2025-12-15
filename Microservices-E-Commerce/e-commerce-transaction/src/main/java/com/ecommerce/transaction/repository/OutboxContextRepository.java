package com.ecommerce.transaction.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.transaction.model.outbox.OutboxEventContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxContextRepository extends R2dbcRepository<OutboxEventContext, UUID> {

 @Modifying
 @Query("""
    SELECT * FROM outbox_event_context
    WHERE outbox_event_id = :eventId
 """)
 Flux<OutboxEventContext> findByOutboxEventId(UUID eventId);

 @Modifying
   @Query("""
      SELECT * FROM outbox_event_context
      WHERE outbox_event_id = :eventId AND field_name = :fieldName
   """)
 Mono<OutboxEventContext> findByOutboxEventFieldName(UUID eventId,String fieldName);
 Flux<OutboxEventContext> findByOutboxEventIdIn(List<UUID> ids);
 Mono<Void> deleteByOutboxEventId(UUID eventId);
 Mono<Void> deleteByOutboxEventIdIn(List<UUID> eventIds);
 //  Flux<OutboxEventContext> findByOrderId(UUID orderId);
}
