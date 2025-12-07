package com.ecommerce.stock.service;

import java.util.UUID;

import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.stock.config.ProductMapper;
import com.ecommerce.stock.event.StockIncreaseRequested;
import com.ecommerce.stock.event.StockRequested;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.dto.ProductInputDTO;
import com.ecommerce.stock.model.dto.ProductQuantityInputDTO;
import com.ecommerce.stock.repository.ProductCacheRepository;
import com.ecommerce.stock.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final RedissonReactiveClient redissonClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final ProductCacheRepository cache;
    private final OutboxService outboxService;

    public ProductService(ProductRepository repo, 
                          RedissonReactiveClient redissonClient, 
                          ProductCacheRepository cache,
                          R2dbcEntityTemplate template,
                          ProductMapper productMapper,
                          OutboxService outboxService) {
        this.productRepository = repo;
        this.redissonClient = redissonClient;
        this.cache = cache;
        this.r2dbcEntityTemplate = template;
        this.productMapper = productMapper;        
        this.outboxService = outboxService;
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
            .onErrorResume(e -> {
                logger.error("Error fetching all products", e);
                return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching all products: " + e.getMessage(), e));})
            .subscribeOn(Schedulers.parallel());                
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
            .onErrorResume(e -> {
                logger.error("Error fetching product {}", id, e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching product " + id + " : " + e.getMessage(), e));});
    }


    public Mono<Product> createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        return productRepository.save(product)
            .flatMap(saved -> cache.save(saved).thenReturn(saved))
            .doOnSuccess(p -> logger.info("Product created successfully: {}", p.getId()))
            .onErrorResume(e -> {
                logger.error("Error creating product: {}", e.getMessage(), e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Product with message: " + e.getMessage(), e));
            });
    }                        

    public Mono<Product> updateProduct(UUID id, ProductInputDTO productDetails) {
        if (productDetails == null) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request body is missing"
            ));
        }

        if (productDetails.name() == null &&
            productDetails.description() == null &&
            productDetails.price() == null &&
            productDetails.category() == null &&
            productDetails.stockQuantity() == null) {

            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "At least one field must be provided for update"
            ));
        }

        logger.info("Updating product {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for update.", id);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .flatMap(existing -> {

                    productMapper.updateProductFromInput(productDetails, existing);

                    return productRepository.save(existing)
                            .flatMap(updated ->
                                    cache.save(updated).thenReturn(updated)
                            );
                })
                .doOnSuccess(p -> logger.info("Updated product {}", p.getId()))
                .onErrorResume(e -> {
                    logger.error("Error updating product: {}", e.getMessage(), e);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Updating Product with message: " + e.getMessage(), e));
                });
    }


    public Mono<String> deleteProduct(UUID id) {
    logger.info("Deleting product with id: {}", id);
    return productRepository.findById(id)        
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
            .onErrorResume(e -> {
                logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Deleting Product with message: " + e.getMessage(), e));                
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
            .onErrorResume(e -> {
                logger.error("Error fetching products by category " + category, e);
                return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching products by category " + category + " : " + e.getMessage(), e));
            });
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
            .onErrorResume(e -> {
                logger.error("Error fetching products with price between " + minPrice + " and " + maxPrice, e);
                return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching products with price between " + minPrice + " and " + maxPrice + " : " + e.getMessage(), e));
            });
    }

    public Mono<Boolean> buyProduct(UUID id, int quantity) {
        logger.info("Buying product with id: {} and quantity: {}", id, quantity);
        return productRepository.findById(id)                
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
                .onErrorResume(e -> {
                    logger.error("Error buying product: {}", e.getMessage(), e);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Buying Product with message: " + e.getMessage(), e));
                });
    }

    public Mono<Void> handle(StockRequested event) {
        return outboxService.findByOrderId(event.orderId())
            .flatMap(existing -> {
                logger.info("Event already processed for saga {}", event.sagaId());
                return Mono.empty();
            })
            .switchIfEmpty(
                this.buyProducts(event)                    
            )
            .then();
    }

    public Mono<Void> handleIncrease(StockIncreaseRequested event) {
        return outboxService.findByOrderId(event.orderId())
            .flatMap(existing -> {
                logger.info("Stock increase event already processed for saga {}", event.sagaId());
                return Mono.empty();
            })
            .switchIfEmpty(
                this.processNewStockIncreaseRequest(event)
            )
            .then();
    }

    public Mono<Void> processNewStockIncreaseRequest(StockIncreaseRequested event) {
        logger.info("Processing stock increase for product {} (saga: {})",
            event.orderId(), event.sagaId());
        return Flux.fromIterable(event.productsQuantity())
        .flatMap(entry -> increaseStock(entry.productId(), entry.quantity(), event.sagaId(), event.orderId()))
        .then();
    }

    public Mono<Void> processNewStockRequest(StockRequested event) {
        logger.info("Processing stock request for order {} (saga: {})",
            event.orderId(), event.sagaId());
        return this.buyProducts(event).then();
    }

    public Mono<Void> buyProducts(StockRequested event) {
        if (event.productsQuantity() == null || event.productsQuantity().isEmpty()) {
            logger.warn("Order request is empty or invalid.");
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid order request: missing products."
            ));
        }
        return Flux.fromIterable(event.productsQuantity())
            .flatMap(entry -> processSingleProduct(event, entry))
            .then(); 
        }
    private Mono<Void> processSingleProduct(StockRequested event, ProductQuantityInputDTO entry) {

        return outboxService.saveOutbox(        
            event.sagaId(),
            event.orderId(),
            entry.productId(),
            entry.quantity(),
            null,
            "StockRequested"
        ).map(saved -> saved.getId())
        .flatMap(id -> {
            return productRepository.tryDecreaseStock(entry.productId(), entry.quantity())
                .flatMap(updated ->
                    cache.save(updated)                    
                        .then(outboxService.updateOutbox(
                                id,
                                event.sagaId(),
                                event.orderId(),
                                updated.getId(),
                                entry.quantity(),
                                null,
                                "StockReserved"
                        ))
                        .doOnSuccess(v -> logger.info(
                                "Stock decreased for product {} by quantity {}",
                                entry.productId(), entry.quantity()
                                )
                        )
                )
                .switchIfEmpty(
                    productRepository.findById(entry.productId())
                    .flatMap(existing ->
                        outboxService.updateOutbox(
                            id,
                            event.sagaId(),
                            event.orderId(),
                            existing.getId(),
                            entry.quantity(),                                
                            "Insufficient stock for product: " + existing.getName(),
                            "StockRejected"
                        )
                        .doOnSuccess(v -> logger.warn(
                                "Insufficient stock for product {}", existing.getName()))
                    )                           
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            logger.warn("Product not found with id {}", entry.productId());

                            return outboxService.updateOutbox(
                                id,
                                event.sagaId(),
                                event.orderId(),
                                entry.productId(),
                                entry.quantity(),                                    
                                "Product not found with id: " + entry.productId(),
                                "StockRejected"
                            );
                        })
                    ))            
                .onErrorResume(e -> {
                    logger.error("Error processing product {}: {}", entry.productId(), e.getMessage());

                    return outboxService.updateOutbox(
                                id,                                
                                event.sagaId(),
                                event.orderId(),
                                entry.productId(),
                                entry.quantity(),                                
                                "Error: " + e.getMessage(),
                                "StockRejected"
                            
                        );
                });
            }).then();            
    }    

    



    public Mono<Boolean> increaseStock(UUID id, int quantity, UUID sagaId, UUID orderId) {
        logger.info("Increasing stock for product with id: {} by quantity: {}", id, quantity);
        
        return productRepository.findById(id)                
                .flatMap(product -> {
                    product.increaseStock(quantity);
                    productRepository.save(product);
                    
                    outboxService.saveOutbox(                            
                            sagaId,
                            orderId,
                            product.getId(),
                            quantity,
                            null,
                            "StockIncreaseApproved"
                    );

                    logger.info("Stock for product with id {} increased successfully.", id);
                    return Mono.just(true);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Product with id {} not found for stock increase.", id);
                    outboxService.saveOutbox(                            
                            sagaId,
                            orderId,
                            id,
                            null,
                            "Product not found with id: " + id,
                            "StockIncreaseRejected"
                    );
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with "+ id + " not found"));
                }))
                .onErrorResume(e -> {
                    logger.error("Error increasing stock: {}", e.getMessage(), e);
                    outboxService.saveOutbox(                            
                            sagaId,
                            orderId,
                            id,
                            quantity,
                            "Error: " + e.getMessage(),
                            "StockIncreaseRejected"
                    );
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Increasing Stock with message: " + e.getMessage(), e));
                });
    }

        


    @Scheduled(fixedRate = 15*60*1000)
    @CacheEvict(value="products", allEntries = true)
    public void clearCache() {
        logger.info("Clearing products cache"); 
        System.out.println("Products Cache was cleared");
    } 

    
    // public Mono<Void> buyProducts(List<ProductQuantityInputDTO> order) {
    // if (order == null || order.isEmpty()) {
    //     logger.warn("Order request is empty or invalid.");
    //     return Mono.error(new ResponseStatusException(
    //         HttpStatus.BAD_REQUEST,
    //         "Invalid order request: missing products."
    //     ));
    // }

    // return Flux.fromIterable(order.stream()
    //         .collect(Collectors.toMap(
    //             ProductQuantityInputDTO::productId,
    //             ProductQuantityInputDTO::quantity
    //         ))
    //         .entrySet())
    //     .flatMap(entry -> {
    //         UUID productId = entry.getKey();
    //         Integer quantityRequested = entry.getValue();

    //         return productRepository.tryDecreaseStock(productId, quantityRequested)
    //             .flatMap(updated -> cache.save(updated)
    //                 .thenReturn(new OrderResult(true,
    //                     "Order successful for product: " + updated.getName(),
    //                     updated))
    //             )
    //             .switchIfEmpty(
    //                 productRepository.findById(productId)
    //                     .map(existing -> new OrderResult(false,
    //                         "Insufficient stock for product: " + existing.getName(),
    //                         existing))
    //                     .switchIfEmpty(Mono.just(new OrderResult(false,
    //                         "Product not found with id: " + productId, new Product())))
    //             )
    //             .onErrorResume(e -> {
    //                 logger.error("Error processing product {}: {}", productId, e.getMessage());
    //                 Product errorProduct = new Product();
    //                 errorProduct.setNotFound();
    //                 return Mono.just(new OrderResult(false,
    //                     "Error: " + e.getMessage(), errorProduct));
    //             });
    //     });
    // }

}