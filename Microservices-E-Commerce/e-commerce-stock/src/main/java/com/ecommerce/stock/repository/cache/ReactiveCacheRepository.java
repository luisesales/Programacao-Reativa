package com.ecommerce.stock.repository.cache;

import java.time.Duration;

import org.redisson.api.RBucketReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class ReactiveCacheRepository<T> {

    private final RedissonReactiveClient redissonReactiveClient;
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    public ReactiveCacheRepository(RedissonReactiveClient redissonReactiveClient) {
        this.redissonReactiveClient = redissonReactiveClient;
    }

    private String getKey(String namespace, String id) {
        return "/" + namespace + "/" + id;
    }

    public Mono<Void> save(String namespace, String id, T value) {
        String key = getKey(namespace, id);
        RBucketReactive<T> bucket = redissonReactiveClient.getBucket(key);
        return bucket.set(value, DEFAULT_TTL).then();
    }

    public Mono<T> find(String namespace, String id) {
        String key = getKey(namespace, id);
        RBucketReactive<T> bucket = redissonReactiveClient.getBucket(key);
        return bucket.get();
    }

    public Mono<Void> delete(String namespace, String id) {
        String key = getKey(namespace, id);
        return redissonReactiveClient.getBucket(key).delete().then();
    }

    public Mono<Void> clearNamespace(String namespace) {
        String pattern = "/" + namespace + "/*";
        return redissonReactiveClient.getKeys().deleteByPattern(pattern).then();
    }
}
