package com.ecommerce.stock.controller;

import java.util.List;
import java.util.Optional;

import org.redisson.api.RedissonReactiveClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.service.ProductService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private RedissonReactiveClient redissonReactiveClient;

    @Autowired
    private ProductService productService;

    @GetMapping
    public Flux<Product> getAllProducts() {
        logger.info("Request received to get all products.");    
        return productService.getAllProducts();
    }

    @GetMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> getProductById(@PathVariable Long id) {
        logger.info("Request received to get product with id: {}", id);
        return productService.getProductById(id);
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> createProduct(@RequestBody Product product) {
        logger.info("Request received to create new product: {}", product.getName());
        Mono<Product> createdProduct = productService.createProduct(product);
        createdProduct.subscribe(p -> 
            logger.info("Product created successfully: {}", p.getName())
        );
        return createdProduct;                
    }

    @PutMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        logger.info("Request received to update product with id: {}", id);
        return productService.updateProduct(id, productDetails)
                             .doOnSuccess(updatedProduct -> 
                                 logger.info("Product with id {} updated successfully.", updatedProduct.getId())
                             );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        logger.info("Request recieved to delete product with id: {}", id);
        if(productService.deleteProduct(id)) {
            logger.info("Product with id {} deleted successfully.", id);
            return ResponseEntity.ok("Product deleted successfully.");
        } else {
            logger.warn("Product with id {} not found for deletion.", id);
            return ResponseEntity.notFound().build();                            
        }
    }

    @PostMapping("/order")
    public ResponseEntity<String> orderProduct(@RequestBody Order order){
        logger.info("Request received to order products :{}", order.getName() + " with price: " + order.getTotalPrice());
        if(order == null || order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
            logger.warn("Order request is empty or invalid.");
            return ResponseEntity.badRequest().body("Invalid order request.");
        }       
        logger.info("Order processed successfully for products: {}", order.getProductsQuantity());        
        return ResponseEntity.ok(productService.buyProducts(order));        
    }
}
