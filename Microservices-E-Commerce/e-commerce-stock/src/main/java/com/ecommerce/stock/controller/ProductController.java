package com.ecommerce.stock.controller;


import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.OrderResult;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private RedissonReactiveClient redissonReactiveClient;

    @Autowired
    private ProductService productService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getAllProducts() {
        logger.info("Request received to get all products.");    
        return productService.getAllProducts();
    }

    @GetMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> getProductById(@PathVariable String id) {
        logger.info("Request received to get product with id: {}", id);
        return productService.getProductById(id);
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> createProduct(@RequestBody Mono<Product> monoProduct) {
        return monoProduct
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Product body is missing"
            )))
            .flatMap(product -> {
                logger.info("Request received to create new product: {}", product.getName());
                return productService.createProduct(product)
                    .doOnSuccess(createdProduct -> {
                        logger.info("Product created successfully with id: {}", createdProduct.getId());
                    })         
                    .doOnError(e -> 
                        logger.error("Error creating product: {}", e.getMessage(), e)
                    );
            }
        );        
    }

    @PutMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> updateProduct(@PathVariable String id, @RequestBody Mono<Product> monoProductDetails) {
        return monoProductDetails
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Product details body is missing"
            )))
            .flatMap(productDetails -> {
            
                logger.info("Request received to update product with id: {}", id);
                return productService.updateProduct(id, productDetails)
                                    .doOnSuccess(updatedProduct -> 
                                        logger.info("Product with id {} updated successfully.", updatedProduct.getId())
                                    )
                                    .doOnError(e -> 
                                        logger.error("Error updating product with id {}: {}", id, e.getMessage(), e)
                                    );
             });
    }

    @DeleteMapping(path = "/{id}" , produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<String> deleteProduct(@PathVariable String id) {
        logger.info("Request received to delete product with id: {}", id);
        return productService.deleteProduct(id)
                                .doOnSuccess(message -> 
                                    logger.info("Product with id {} deleted successfully.", id)
                                )
                                .doOnError(e -> 
                                    logger.error("Error deleting product with id {}: {}", id, e.getMessage(), e)
                                );
    }

    @PostMapping(path = "/order", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OrderResult> orderProduct(@RequestBody Mono<Order> monoOrder) {
        return monoOrder
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
            .flatMapMany(order -> {
                logger.info("Request received to order products: {} with price: {}",
                        order.getName(), order.getTotalPrice());

                if (order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
                    logger.warn("Order request is empty or invalid.");
                    return Flux.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid order request: missing products."
                    ));
                }

                logger.info("Order processed successfully for products: {}", order.getProductsQuantity());
                return productService.buyProducts(order); 
            });
    }
}
