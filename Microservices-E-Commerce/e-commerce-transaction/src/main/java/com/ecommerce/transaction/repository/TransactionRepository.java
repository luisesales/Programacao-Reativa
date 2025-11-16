package com.ecommerce.transaction.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.transaction.model.Transaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends R2dbcRepository<Transaction, UUID> {
    Mono<Transaction> findById(UUID id);
    Mono<Void> deleteById(UUID id);
    Mono<Transaction> findByName(String name);
    Mono<Transaction> save(Transaction transaction);
    Flux<Transaction> findByOrderId(UUID orderId);
}   
