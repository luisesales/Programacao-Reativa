package com.ecommerce.stock.repository;

import java.util.List;
import java.util.UUID;

import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductCacheRepository {

    private static final String CACHE_NAMESPACE = "products:all";
    private static final String CATEGORY_PREFIX = "products:category:";
    private static final String PRICE_PREFIX = "products:price:";

    private final RedissonReactiveClient redisson;

    public ProductCacheRepository(RedissonReactiveClient redisson) {
        this.redisson = redisson;
    }

    public Mono<Void> save(Product product) {
        if (product == null || product.getId() == null) {
            return Mono.empty();
        }
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return map.fastPut(product.getId().toString(), product).then();
    }

    public Mono<Void> saveAll(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Mono.empty();
        }
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return Flux.fromIterable(products)
                   .flatMap(p -> map.fastPut(p.getId().toString(), p))
                   .then();
    }

    public Flux<Product> findAll() {
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return map.readAllValues()
                  .flatMapMany(Flux::fromIterable)
                  .switchIfEmpty(Flux.empty());
    }

    public Mono<Product> findById(UUID id) {
        if (id == null) return Mono.empty();
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return map.get(id.toString());
    }

    public Mono<Void> delete(UUID id) {
        if (id == null) return Mono.empty();
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return map.remove(id.toString()).then();
    }

    public Mono<Void> saveByCategory(String category, List<Product> products) {
        if (category == null || products == null || products.isEmpty()) return Mono.empty();
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CATEGORY_PREFIX + category);
        return Flux.fromIterable(products)
                   .flatMap(p -> map.fastPut(p.getId().toString(), p))
                   .then();
    }

    public Flux<Product> findByCategory(String category) {
        if (category == null) return Flux.empty();
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CATEGORY_PREFIX + category);
        return map.readAllValues().flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> saveByPriceRange(Double minPrice, Double maxPrice, List<Product> products) {
        if (products == null || products.isEmpty()) return Mono.empty();
        String key = String.format("%s%s-%s", PRICE_PREFIX, minPrice, maxPrice);
        RMapCacheReactive<String, Product> map = redisson.getMapCache(key);
        return Flux.fromIterable(products)
                   .flatMap(p -> map.fastPut(p.getId().toString(), p))
                   .then();
    }

    public Flux<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        String key = String.format("%s%s-%s", PRICE_PREFIX, minPrice, maxPrice);
        RMapCacheReactive<String, Product> map = redisson.getMapCache(key);
        return map.readAllValues().flatMapMany(Flux::fromIterable);
    }

    public Mono<Void> clearAll() {
        RMapCacheReactive<String, Product> map = redisson.getMapCache(CACHE_NAMESPACE);
        return map.delete().then();
    }
}