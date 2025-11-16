package com.ecommerce.stock.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.OrderResult;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Controller
public class ProductControllerGraphQL {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);


    @Autowired
    private ProductService productService;

    @QueryMapping
    public Flux<Product> getAllProducts() {
        logger.info("Request received to get all products.");    
        return productService.getAllProducts();
    }

    @QueryMapping
    public Mono<Product> getProductById(@Argument UUID id) {
        logger.info("Request received to get product with id: {}", id);
        return productService.getProductById(id);
    }

    @QueryMapping
    public Flux<Product> getProductByCategory(@Argument String param) {
        logger.info("Request received to get products in category: {}", param);
        return productService.findByCategory(param);
    }
    

    @MutationMapping
    public Mono<Product> createProduct(@Argument Mono<Product> monoProduct) {
        return monoProduct
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Product body is missing"
            )))
            .flatMap(product -> {
                logger.info("Request received to create new product: {}", product.getName());
                return productService.createProduct(product);
                    
            }
        );       
    }

    @MutationMapping
    public Mono<Product> updateProduct(@Argument UUID id, @Argument Product monoProductDetails) {
        if (monoProductDetails == null) {
        return Mono.error(new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Product details body is missing"
            ));
        }

        logger.info("Request received to update product with id: {}", id);

        return productService.updateProduct(id, monoProductDetails)
            .doOnSuccess(updatedProduct ->
                logger.info("Product with id {} updated successfully.", updatedProduct.getId())
            )
            .doOnError(e ->
                logger.error("Error updating product with id {}: {}", id, e.getMessage(), e)
            );
    }

    @MutationMapping
    public Mono<String> deleteProduct(@Argument UUID id) {
        logger.info("Request received to delete product with id: {}", id);
        return productService.deleteProduct(id);
                                
    }

    @MutationMapping
    public Flux<OrderResult> orderProduct(@Argument Mono<Order> monoOrder) {
        return monoOrder
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
            .flatMapMany(order -> {
                logger.info("Request received to order products: {} with price: {}", order.getName(), order.getTotalPrice());                
                return productService.buyProducts(order); 
            });
    }
}
