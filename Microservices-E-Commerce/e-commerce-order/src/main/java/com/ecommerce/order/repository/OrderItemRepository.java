package com.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.OrderItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderItemRepository extends R2dbcRepository<OrderItem, UUID> {
    Mono<OrderItem> findById(UUID id);
    Mono<Void> deleteById(UUID id);
    Flux<OrderItem> findByOrderId(UUID orderId);
}   