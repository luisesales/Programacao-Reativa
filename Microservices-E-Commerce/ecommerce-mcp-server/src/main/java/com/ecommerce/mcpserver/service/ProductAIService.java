package com.ecommerce.mcpserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ecommerce.mcpserver.http.ProductHttpInterface;
import com.ecommerce.mcpserver.http.ProductHttpInterfaceFallback;
import com.ecommerce.mcpserver.model.Product;
import com.ecommerce.mcpserver.model.Order;

@Service
public class ProductAIService {

    private final ProductHttpInterface productHttpInterface;
    private final ProductHttpInterfaceFallback fallback;

    public ProductAIService(@Qualifier("productHttpInterfaceImpl") ProductHttpInterface productHttpInterface,
                            @Qualifier("productHttpInterfaceFallback") ProductHttpInterfaceFallback fallback) {
        this.productHttpInterface = productHttpInterface;
        this.fallback = fallback;
    }

    publcic List<Product> getAllProducts() {
        try {
            return productHttpInterface.getAllProducts();
            return response.getBody();
        } catch (Exception e) {
            return fallback.getAllProducts();
        }
    }
    public Optional<Product> getProductById(Long id) {
        try {
            ResponseEntity<Product> response = productHttpInterface.getProductById(id);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            return fallback.getProductById(id);
        }
    }
    public String orderProduct(Order order) {
        try {
            ResponseEntity<String> response = productHttpInterface.orderProduct(order);
            return response.getBody();
        } catch (Exception e) {
            return fallback.orderProduct(order);
        }
    }
}