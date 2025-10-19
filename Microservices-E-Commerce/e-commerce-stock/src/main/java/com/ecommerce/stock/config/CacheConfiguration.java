package com.ecommerce.stock.config;

import java.util.HashMap;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.spring.cache.CacheConfig;
import java.util.Map;

@Configuration
public class CacheConfiguration {
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        config.put("products", new CacheConfig(30 * 60 * 1000, 15 * 60 * 1000)); // TTL 30 min, MaxIdle 15 min
        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
