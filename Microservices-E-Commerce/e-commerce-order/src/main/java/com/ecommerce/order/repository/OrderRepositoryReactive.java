package com.ecommerce.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.ecommerce.order.model.Order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepositoryReactive extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAll();
    Mono<Order> findById(Long id);
}
