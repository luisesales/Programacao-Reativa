package com.ecommerce.order.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.saga.SagaContextProductsQuantity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SagaContextProductsQuantityRepository extends R2dbcRepository<SagaContextProductsQuantity, UUID> {
    Flux<SagaContextProductsQuantity> findBySagaContextId(UUID sagaContextId);
    Flux<SagaContextProductsQuantity> findByProductId(UUID productId);
    Mono<Void> saveAll(List<SagaContextProductsQuantity> list);
    @Query("""
        SELECT * FROM saga_context_products_id 
        where saga_context_id = :sagaContextId AND product_id = :productId
    """)
    Mono<SagaContextProductsQuantity> findBySagaContextIdProdId(UUID sagaContextId, UUID productId);
}