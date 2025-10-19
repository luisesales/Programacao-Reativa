package com.ecommerce.order.repository;

import com.ecommerce.order.model.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface OrderRepository extends R2dbcRepository<Order, String> {
    Flux<Order> findAll();
    Mono<Order> findById(String id);
}
