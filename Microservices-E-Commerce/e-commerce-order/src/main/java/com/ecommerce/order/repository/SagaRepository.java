package com.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.saga.SagaInstance;

import reactor.core.publisher.Mono;

public interface SagaRepository extends R2dbcRepository<SagaInstance, UUID> {

    Mono<SagaInstance> findBySagaId(UUID sagaId);
}