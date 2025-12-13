package com.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.saga.SagaContext;

import reactor.core.publisher.Mono;

public interface SagaContextRepository extends R2dbcRepository<SagaContext, UUID> {
    Mono<SagaContext> findBySagaId(UUID sagaId);
    Mono<SagaContext> findByOrderId(UUID orderId);
    Mono<SagaContext> findByTransactionId(UUID transactionId);
}