package com.ecommerce.stock.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends R2dbcRepository<Product, UUID> {
    Mono<Product> findByName(String name);
    Flux<Product> findByCategory(String category);
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    Mono<Product> findById(UUID id);
    Mono<Void> deleteById(UUID id);
}