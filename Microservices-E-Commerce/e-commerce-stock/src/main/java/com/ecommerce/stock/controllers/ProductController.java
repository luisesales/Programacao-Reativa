package com.ecommerce.stock.controllers;

import java.util.List;
import java.util.Optional;

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

import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.service.ProductService;

@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts() {
        logger.info("Request received to get all products.");    
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("Request received to get product with id: {}", id);
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.info("Request received to create new product: {}", product.getName());
        Product createdProduct = productService.createProduct(product);
        logger.info("Product created successfully: {}", createdProduct.getName());
        return ResponseEntity.ok(createdProduct);                
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        logger.info("Request received to update product with id: {}", id);
        return productService.getProductById(id)
                .map(product -> {
                    Product updatedProduct = productService.updateProduct(id, product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        logger.info("Deleting product with id: {}", id);
        return productService.getProductById(id)
                .map(product -> {
                    if(productService.deleteProduct(product)) {
                        logger.info("Product with id {} deleted successfully.", id);
                        return ResponseEntity.ok("Product deleted successfully.");
                    } else {
                        logger.warn("Product with id {} not found for deletion.", id);
                        return ResponseEntity.notFound().build();
                    }                    
                })
                
    }


}
