package com.ecommerce.order.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.saga.SagaInstance;

import reactor.core.publisher.Mono;

public interface SagaRepository extends R2dbcRepository<SagaInstance, String> {

    Mono<SagaInstance> findBySagaId(String sagaId);

    Mono<SagaInstance> findByOrderId(String orderId);
}