package com.ecommerce.stock.repository;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ProductRepository extends R2dbcRepository<Product, Long> {
    Mono<Product> findByName(String name);
    Mono<Product> findById(Long id);
    Flux<Product> findByCategory(String category);
    Flux<Product> findAll();
    Mono<Product> save(Product product);    
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
