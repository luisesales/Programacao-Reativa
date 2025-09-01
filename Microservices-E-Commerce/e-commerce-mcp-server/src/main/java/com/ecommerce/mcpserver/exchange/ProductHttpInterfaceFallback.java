package com.ecommerce.mcpserver.exchange;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.mcpserver.model.Order;
import com.ecommerce.mcpserver.model.Product;

@Component
public class ProductHttpInterfaceFallback implements ProductHttpInterface {

    private static final Logger logger = LoggerFactory.getLogger(ProductHttpInterfaceFallback.class);

    @Override
    public ResponseEntity<List<Product>> getAllProducts(){
        logger.warn("Fallback added to getAllProducts: {}. Returning empty list.");
        return ResponseEntity.status(503).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.warn("Fallback added to getProductById for id: {}. Returning empty Optional.", id);
        return ResponseEntity.status(503).body(new Product());
    }

    @Override
    public ResponseEntity<String> orderProduct(@RequestBody Order order) {
        logger.warn("Fallback added to orderProduct. Returning error message.");
        return ResponseEntity.status(503).body("The product ordering service is currently unavailable.");
    }

    @Override
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.warn("Fallback added to createProduct. Returning empty product.");
        return ResponseEntity.status(503).body(new Product());
    }
}