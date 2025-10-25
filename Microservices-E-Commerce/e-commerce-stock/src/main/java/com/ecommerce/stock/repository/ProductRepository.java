package com.ecommerce.stock.repository;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import java.util.UUID;

public interface ProductRepository extends R2dbcRepository<Product, UUID> {
    Mono<Product> findByName(String name);
    Flux<Product> findByCategory(String category);
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    Mono<Product> findById(UUID id);
}

