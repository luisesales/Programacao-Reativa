package com.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.saga.SagaContext;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface SagaContextProductsQuantityRepository extends R2dbcRepository<SagaContext, UUID> {
    Mono<SagaContext> findBySagaContextId(UUID sagaContextId);
    Flux<SagaContext> findByProductId(UUID productId);
}