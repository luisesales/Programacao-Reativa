package com.ecommerce.stock.repository;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ProductRepository extends R2dbcRepository<Product, String> {
    Mono<Product> findByName(String name);
    Mono<Product> findById(String id);
    Flux<Product> findByCategory(String category);
    Flux<Product> findAll();
    Mono<Product> save(Product product);    
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
