package com.ecommerce.order.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.order.model.Order;

import reactor.core.publisher.Mono;

public interface OrderRepository extends R2dbcRepository<Order, UUID> {
    Mono<Order> findById(UUID id);
    Mono<Void> deleteById(UUID id);
    Mono<Order> findByName(String name);
    Mono<Order> save(Order order);
}   
