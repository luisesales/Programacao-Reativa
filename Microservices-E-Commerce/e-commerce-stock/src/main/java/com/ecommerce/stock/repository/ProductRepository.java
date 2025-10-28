package com.ecommerce.stock.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository extends R2dbcRepository<Product, UUID> {
    Mono<Product> findByName(String name);
    Flux<Product> findByCategory(String category);
    Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice);
    Mono<Product> findById(UUID id);
    Mono<Void> deleteById(UUID id);

    @Query("""
        UPDATE product 
        SET stock_quantity = stock_quantity - :quantity
        WHERE id = :productId AND stock_quantity >= :quantity
        RETURNING *
    """)
    Mono<Product> tryDecreaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);

    @Query("""
        UPDATE product 
        SET stock_quantity = stock_quantity + :quantity
        WHERE id = :productId
        RETURNING *
    """)
    Mono<Product> increaseStock(@Param("productId") UUID productId, @Param("quantity") int quantity);
}