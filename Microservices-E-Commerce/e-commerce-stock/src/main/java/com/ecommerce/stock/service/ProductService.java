package com.ecommerce.stock.service;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.OrderResult;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedissonReactiveClient redissonClient;

    

    @Cacheable(value = "products")
    public Flux<Product> getAllProducts() {        
        logger.info("Fetching all products");
        return productRepository.findAll()
                                .publishOn(Schedulers.boundedElastic())
                                .doOnError(e -> logger.error("Error fetching all products", e));
    }
    
    @Cacheable(value = "products", key = "#id")
    public Mono<Product> getProductById(Long id) {
        logger.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                                .publishOn(Schedulers.boundedElastic())
                                .doOnError(e -> logger.error("Error fetching product id " + id, e))
                                .switchIfEmpty(Mono.defer(() -> {
                                    logger.warn("Product with id {} not found.", id);
                                    return Mono.empty();
                                }));
        
    }

    @CacheEvict(value = "products", key = "#product.id")
    public Mono<Product> createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        return productRepository.save(product)
                .publishOn(Schedulers.boundedElastic())
                .doOnError(e -> logger.error("Error creating product: {}", e.getMessage(), e));
    }
    
    @CacheEvict(value = "products", key = "#id")
    public Mono<Product> updateProduct(Long id, Product productDetails) {
        logger.info("Updating product with id: {}", id);
        return productRepository.findById(id)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setStockQuantity(productDetails.getStockQuantity());
                    product.setCategory(productDetails.getCategory());
                    product.setStockQuantity(productDetails.getStockQuantity());
                    logger.info("Product with id {} updated successfully.", id);                    
                    return productRepository.save(product);                    
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for update.", id);
                    return Mono.empty();
                }))
                .doOnError(e -> logger.error("Error updating oproduct: {}", e.getMessage(), e));
    }

    @CacheEvict(value = "products", key = "#id")
    public Mono<String> deleteProduct(Long id) {
    logger.info("Deleting product with id: {}", id);
    return productRepository.findById(id)
            .publishOn(Schedulers.boundedElastic())
            .flatMap(product -> {
                logger.info("Product with id {} found for deletion.", id);
                return productRepository.delete(product)
                        .then(Mono.just("Product with id {} found for deletion." + id));
            })
            .switchIfEmpty(Mono.defer(() -> {
                logger.warn("Product with id {} not found for deletion.", id);
                return Mono.just("Product id {} not found for deletion." + id);
            }));
    }

    @Cacheable(value = "products", key = "#category")
    public Flux<Product> findByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
        return productRepository.findByCategory(category)
                                .publishOn(Schedulers.boundedElastic())
                                .doOnError(e -> logger.error("Error fetching products by category " + category, e));
    }

    @Cacheable(value = "products", key = "{#minPrice, #maxPrice}")
    public Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice) {
        logger.info("Fetching products with price between {} and {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice)
                                .publishOn(Schedulers.boundedElastic())
                                .doOnError(e -> logger.error("Error fetching products with price between " + minPrice + " and " + maxPrice, e));
    }

    @CacheEvict(value = "products", key = "#id")
    public Mono<Boolean> buyProduct(Long id, int quantity) {
        logger.info("Buying product with id: {} and quantity: {}", id, quantity);
        return productRepository.findById(id)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(product -> {
                    if (product.getStockQuantity() >= quantity) {
                        product.decreaseStock(quantity);
                        productRepository.save(product);
                        logger.info("Product with id {} bought successfully.", id);
                        return Mono.just(true);
                    } else {
                        logger.warn("Insufficient stock for product with id {}.", id);
                        return Mono.just(false);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for deletion.", id);
                    return Mono.just(false);
                }))
                .doOnError(e -> logger.error("Error buying product: {}", e.getMessage(), e));
    }
    
    public Flux<OrderResult> buyProducts(Order order) {
    return Flux.fromIterable(order.getProductsQuantity().entrySet())
        .flatMap(entry -> {
            Long productId = entry.getKey();
            Integer quantityRequested = entry.getValue();

            String lockKey = "lock:product:" + productId;
            RLockReactive lock = redissonClient.getLock(lockKey);

            return lock.tryLock()
                .flatMap(locked -> {
                    if (!locked) {
                        OrderResult result = new OrderResult();
                        result.setProduct(new Product());                     
                        result.setSuccess(false);
                        result.setResponse("Could not acquire lock for product " + productId);
                        return Mono.just(result);
                    }

                    return productRepository.findById(productId)
                        .defaultIfEmpty(new Product()) 
                        .flatMap(product -> {
                            OrderResult result = new OrderResult();
                            if (product.getId() == null) {
                                result.setProduct(new Product());
                                result.getProduct().setNotFound();
                                result.setSuccess(false);
                                result.setResponse("Product not found with id: " + productId);
                                return Mono.just(result);
                            }

                            if (product.getStockQuantity() >= quantityRequested) {
                                product.decreaseStock(quantityRequested);
                                return productRepository.save(product)
                                        .map(saved -> {
                                            result.setProduct(saved);
                                            result.setSuccess(true);
                                            result.setResponse("Order successful for product: " + saved.getName());
                                            return result;
                                        });
                            } else {
                                result.setProduct(product);
                                result.setSuccess(false);
                                result.setResponse("Insufficient stock for product: " + product.getName());
                                return Mono.just(result);
                            }
                        })
                        .flatMap(result ->
                            lock.unlock()
                                .thenReturn(result)
                        );
                });
        });
    }


    @CacheEvict(value = "products", key = "#id")
    public Mono<Boolean> increaseStock(Long id, int quantity) {
        logger.info("Increasing stock for product with id: {} by quantity: {}", id, quantity);
        return productRepository.findById(id)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(product -> {
                    product.increaseStock(quantity);
                    productRepository.save(product);
                    logger.info("Stock for product with id {} increased successfully.", id);
                    return Mono.just(true);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for stock increase.", id);
                    return Mono.just(false);
                }))
                .doOnError(e -> logger.error("Error increasing stock: {}", e.getMessage(), e));
    }

    @Scheduled(fixedRate = 10_000)
    @CacheEvict(value="products", allEntries = true)
    public void clearCache() {
        System.out.println("Products Cache was cleared");
    } 
}
