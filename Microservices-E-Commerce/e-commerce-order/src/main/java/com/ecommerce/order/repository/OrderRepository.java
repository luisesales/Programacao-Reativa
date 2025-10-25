package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface OrderRepository extends R2dbcRepository<Order, UUID> {
    Flux<Order> findAll();
    Mono<Order> findById(UUID id);
}
