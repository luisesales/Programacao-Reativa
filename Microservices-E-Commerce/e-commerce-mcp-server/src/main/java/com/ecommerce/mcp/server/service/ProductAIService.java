package com.ecommerce.mcp.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.mcp.server.exchange.ProductHttpInterface;
import com.ecommerce.mcp.server.exchange.ProductHttpInterfaceFallback;
import com.ecommerce.mcp.server.model.Order;
import com.ecommerce.mcp.server.model.Product;

@Service
public class ProductAIService {

    private final ProductHttpInterface productHttpInterface;
    private final ProductHttpInterfaceFallback fallback;

    // public ProductAIService(@Qualifier("productsHttpInterface") ProductHttpInterface productHttpInterface,
    //                         @Qualifier("productHttpInterfaceFallback") ProductHttpInterfaceFallback fallback) {
    //     this.productHttpInterface = productHttpInterface;
    //     this.fallback = fallback;
    // }

    public ProductAIService(ProductHttpInterface productHttpInterface,
                            ProductHttpInterfaceFallback fallback) {
        this.productHttpInterface = productHttpInterface;
        this.fallback = fallback;
    }

    public List<Product> getAllProducts() {
        try {
            ResponseEntity<List<Product>> response = productHttpInterface.getAllProducts();
            return response.getBody();
        } catch (Exception e) {
            return fallback.getAllProducts().getBody();
        }
    }
    public Optional<Product> getProductById(String id) {
        try {
            ResponseEntity<Product> response = productHttpInterface.getProductById(id);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            return Optional.ofNullable(fallback.getProductById(id).getBody());
        }
    }
    public String orderProduct(Order order) {
        try {
            ResponseEntity<String> response = productHttpInterface.orderProduct(order);
            return response.getBody();
        } catch (Exception e) {
            return fallback.orderProduct(order).getBody();
        }
    }

    public Product createProduct(Product product) {
        try {
            ResponseEntity<Product> response = productHttpInterface.createProduct(product);
            return response.getBody();
        } catch (Exception e) {
            return fallback.createProduct(product).getBody();
        }
    }
}