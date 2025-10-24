package com.ecommerce.stock.service;

import org.redisson.api.RLockReactive;
import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public ProductService(R2dbcEntityTemplate template) {
        r2dbcEntityTemplate = template;
    }

    

    @Cacheable(value = "products")
    public Flux<Product> getAllProducts() {        
        logger.info("Fetching all products");
        return productRepository.findAll()
                                .publishOn(Schedulers.boundedElastic())
                                .doOnError(e -> logger.error("Error fetching all products", e));
    }
    
    @Cacheable(value = "products", key = "#id")
    public Mono<Product> getProductById(String id) {
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
        return r2dbcEntityTemplate.insert(Product.class).using(product)
                .publishOn(Schedulers.boundedElastic())
                .doOnError(e -> {
                    logger.error("Error creating product: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Product with message: " + e.getMessage(), e));
                })
                .doOnSuccess(createdProduct -> {
                        logger.info("Product created successfully with id: {}", createdProduct.getId());
                    });         
    }
    
    @CacheEvict(value = "products", key = "#id")
    public Mono<Product> updateProduct(String id, Product productDetails) {
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
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .doOnError(e -> {
                    logger.error("Error updating product: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Updating Product with message: " + e.getMessage(), e));
                });
    }

    @CacheEvict(value = "products", key = "#id")
    public Mono<String> deleteProduct(String id) {
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
            }))
            .doOnSuccess(message -> 
                logger.info("Product with id {} deleted successfully.", id)
            )
            .doOnError(e -> 

                logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e)
            );
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
                                .doOnError(e -> {
                                    logger.error("Error fetching products with price between " + minPrice + " and " + maxPrice, e);
                                    Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching products with price between " + minPrice + " and " + maxPrice + " : " + e.getMessage(), e));
                                });
    }

    @CacheEvict(value = "products", key = "#id")
    public Mono<Boolean> buyProduct(String id, int quantity) {
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
        .flatMap(entry -> {
            String productId = entry.getKey();
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
                        logger.warn("Could not acquire lock for product id: {}", productId);
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
                                logger.warn("Product not found with id: {}", productId);
                                return Mono.just(result);
                            }

                            if (product.getStockQuantity() >= quantityRequested) {
                                product.decreaseStock(quantityRequested);
                                return productRepository.save(product)
                                        .map(saved -> {
                                            result.setProduct(saved);
                                            result.setSuccess(true);
                                            result.setResponse("Order successful for product: " + saved.getName());
                                            logger.info("Order successful for product id: {}", productId);
                                            return result;
                                        });
                            } else {
                                result.setProduct(product);
                                result.setSuccess(false);
                                result.setResponse("Insufficient stock for product: " + product.getName());
                                logger.warn("Insufficient stock for product id: {}", productId);
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
    public Mono<Boolean> increaseStock(String id, int quantity) {
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

    @Scheduled(fixedRate = 15*60*1000)
    @CacheEvict(value="products", allEntries = true)
    public void clearCache() {
        logger.info("Clearing products cache"); 
        System.out.println("Products Cache was cleared");
    } 
}
