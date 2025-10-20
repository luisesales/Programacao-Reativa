package com.ecommerce.mcp.server.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.mcp.server.exchange.ProductHttpInterface;
import com.ecommerce.mcp.server.model.Order;
import com.ecommerce.mcp.server.model.OrderResult;
import com.ecommerce.mcp.server.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductAIService {

    private final ProductHttpInterface productHttpInterface;

    public ProductAIService(ProductHttpInterface productHttpInterface) {
        this.productHttpInterface = productHttpInterface;        
    }

    public Flux<Product> getAllProducts() {
        return productHttpInterface.getAllProducts().onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Returning Products Service is not available", e));
        });   
    }
    public Mono<Product> getProductById(String productId) {
        return productHttpInterface.getProductById(productId).onErrorResume(e -> {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Returning Product with "+ productId + "Service is not available", e));
        });
    }
    public Flux<OrderResult> orderProduct(Order order) {        
        return productHttpInterface.orderProduct(Mono.just(order)).onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Ordering Products for order"+ order.getName() + "Service is not available", e));
        });
    }
    public Mono<Product> createProduct(Product product) {
        return productHttpInterface.createProduct(Mono.just(product)).onErrorResume(e -> {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Creating Product "+ product.getName() + "Service is not available", e));
        });
    }
}