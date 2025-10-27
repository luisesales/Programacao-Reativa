package com.ecommerce.stock.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.OrderResult;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.repository.ProductCacheRepository;
import com.ecommerce.stock.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final RedissonReactiveClient redissonClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final ProductCacheRepository cache;

    public ProductService(ProductRepository repo, 
                          RedissonReactiveClient redissonClient, 
                          ProductCacheRepository cache,
                          R2dbcEntityTemplate template) {
        this.productRepository = repo;
        this.redissonClient = redissonClient;
        this.cache = cache;
        this.r2dbcEntityTemplate = template;
    }

    


    public Flux<Product> getAllProducts() {
        logger.info("Fetching all products (cache-first)");
        return cache.findAll()
            .switchIfEmpty(
                productRepository.findAll()
                    .collectList()
                    .flatMap(products -> cache.saveAll(products).thenReturn(products))
                    .flatMapMany(Flux::fromIterable)                    
            )
            .doOnError(e -> logger.error("Error fetching all products", e))
            .subscribeOn(Schedulers.boundedElastic());
    }
    

    public Mono<Product> getProductById(UUID id) {
        logger.info("Fetching product with id: {}", id);
        return cache.findById(id)
            .switchIfEmpty(
                productRepository.findById(id)
                    .flatMap(product -> cache.save(product).thenReturn(product))                
                    .switchIfEmpty(Mono.defer(() -> {
                        logger.warn("Product with id {} not found.", id);
                        return Mono.empty();
                                }))
            )
            .doOnError(e -> logger.error("Error fetching product {}", id, e))
            .subscribeOn(Schedulers.boundedElastic());
    }


    public Mono<Product> createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        return productRepository.save(product)
            .flatMap(saved -> cache.save(saved).thenReturn(saved))
            .doOnSuccess(p -> logger.info("Product created successfully: {}", p.getId()))
            .doOnError(e -> {
                logger.error("Error creating product: {}", e.getMessage(), e);
                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Product with message: " + e.getMessage(), e));
            })
            .subscribeOn(Schedulers.boundedElastic());
    }                        
    
    public Mono<Product> updateProduct(UUID id, Product productDetails) {
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
                    return productRepository.save(product)                    
                        .flatMap(updated -> cache.save(updated).thenReturn(updated));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for update.", id);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .doOnError(e -> {
                    logger.error("Error updating product: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Updating Product with message: " + e.getMessage(), e));
                });
    }

    public Mono<String> deleteProduct(UUID id) {
    logger.info("Deleting product with id: {}", id);
    return productRepository.findById(id)
            .publishOn(Schedulers.boundedElastic())
            .flatMap(product -> {
                logger.info("Product with id {} found for deletion.", id);
                return productRepository.delete(product)
                    .then(cache.delete(id))
                    .thenReturn("Product with id " + id + " deleted successfully.");
            })                                        
            .switchIfEmpty(Mono.defer(() -> {
                logger.warn("Product with id {} not found for deletion.", id);
                return Mono.just("Product id {} not found for deletion." + id);
            }))
            .doOnSuccess(message -> 
                logger.info("Product with id {} deleted successfully.", id)
            )
            .doOnError(e -> {
                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Deleting Product with message: " + e.getMessage(), e));
                logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e);
            });
    }

    public Flux<Product> findByCategory(String category) {
        logger.info("Fetching products by category: {}", category);
         return cache.findByCategory(category)
            .switchIfEmpty( 
                productRepository.findByCategory(category)
                                .collectList()
                                .flatMap(products -> cache.saveByCategory(category, products).thenReturn(products))
                                .flatMapMany(Flux::fromIterable)                                
            )
            .doOnError(e -> {
                logger.error("Error fetching products by category " + category, e);
                Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching products by category " + category + " : " + e.getMessage(), e));
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<Product> findByPriceBetween(Double minPrice, Double maxPrice) {
        logger.info("Fetching products with price between {} and {}", minPrice, maxPrice);
        return cache.findByPriceRange(minPrice, maxPrice)
            .switchIfEmpty(
                productRepository.findByPriceBetween(minPrice, maxPrice)
                    .collectList()
                    .flatMap(products -> cache.saveByPriceRange(minPrice, maxPrice, products).thenReturn(products))
                    .flatMapMany(Flux::fromIterable)                                    
            )
            .doOnError(e -> {
                logger.error("Error fetching products with price between " + minPrice + " and " + maxPrice, e);
                Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching products with price between " + minPrice + " and " + maxPrice + " : " + e.getMessage(), e));
            })
            .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Boolean> buyProduct(UUID id, int quantity) {
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
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .doOnError(e -> {
                    logger.error("Error buying product: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Buying Product with message: " + e.getMessage(), e));
                });
    }
    
    public Flux<OrderResult> buyProducts(Order order) {
    if (order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
        logger.warn("Order request is empty or invalid.");
        return Flux.error(new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid order request: missing products."
        ));
    }

    logger.info("Order processed successfully for products: {}", order.getProductsQuantity());

    return Flux.fromIterable(order.getProductsQuantity().entrySet())
        .publishOn(Schedulers.boundedElastic())
        .flatMap(entry -> {
            UUID productId = entry.getKey();
            Integer quantityRequested = entry.getValue();            
            RLockReactive lock = redissonClient.getLock("lock:product:" + productId);
            return Mono.usingWhen(
                lock.tryLock(5, 30, TimeUnit.SECONDS)
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not acquire lock for product id: " + productId
                    )))
                .subscribeOn(Schedulers.parallel()),
            locked -> processProduct(productId, quantityRequested)
                .subscribeOn(Schedulers.parallel()),
            locked -> lock.unlock()
                .doOnSuccess(v -> logger.info("Lock released for product {}", productId))
                .doOnError(e -> logger.warn("Failed to release lock for product {}: {}", productId, e.getMessage()))
                .onErrorResume(e -> Mono.empty())
            )
            .onErrorResume(e -> {
                logger.error("Error processing product {}: {}", productId, e.getMessage());
                Product errorProduct = new Product();
                errorProduct.setNotFound();
                return Mono.just(new OrderResult(false, "Error: " + e.getMessage(), errorProduct));
            });
        });
}

    private Mono<OrderResult> processProduct(UUID productId, Integer quantityRequested) {
        return productRepository.findById(productId) 
            // .switchIfEmpty(Mono.defer(() -> {
            //     Product notFound = new Product();
            //     notFound.setNotFound();
            //     return Mono.just(notFound);
            // }))           
            .flatMap(product -> {
                if (product.getId() == null) {
                    product.setNotFound();
                    return Mono.just(new OrderResult(false,
                        "Product not found with id: " + productId, product));
                }

                if (product.getStockQuantity() >= quantityRequested) {
                    product.decreaseStock(quantityRequested);
                    return productRepository.save(product)
                        .flatMap(saved -> cache.save(saved).thenReturn(
                            new OrderResult(true, "Order successful for product: " + saved.getName(), saved)));
                } else {
                    return Mono.just(new OrderResult(false,
                        "Insufficient stock for product: " + product.getName(), product));
                }
            })
            .switchIfEmpty(Mono.just(new OrderResult(false, "Product not found", new Product())));
    }

    public Mono<Boolean> increaseStock(UUID id, int quantity) {
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
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .doOnError(e -> {
                    logger.error("Error increasing stock: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Increasing Stock with message: " + e.getMessage(), e));
                });
    }

    @Scheduled(fixedRate = 1*60*1000)
    @CacheEvict(value="products", allEntries = true)
    public void clearCache() {
        logger.info("Clearing products cache"); 
        System.out.println("Products Cache was cleared");
    } 
}