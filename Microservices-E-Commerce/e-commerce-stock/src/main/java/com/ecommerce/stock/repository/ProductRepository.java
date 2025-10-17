package com.ecommerce.stock.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
    Mono<Product> findByName(String name);
    Mono<Product> findById(Long id);
    Flux<Product> findByCategory(String category);
    Flux<Product> findAll();
    Mono<Product> save(Product product);    
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
