package com.ecommerce.mcp.server.exchange;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.mcp.server.model.Order;

@Component
public class OrderHttpInterfaceFallback implements OrderHttpInterface {
     private static final Logger logger = LoggerFactory.getLogger(OrderHttpInterfaceFallback.class);

    @Override
    public ResponseEntity<List<Order>> getAllOrders(){
        logger.warn("Fallback added to getAllOrders: {}. Returning empty list.");
        return ResponseEntity.status(503).body(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        logger.warn("Fallback added to getOrderById for id: {}. Returning empty Optional.", id);
        return ResponseEntity.status(503).body(new Order());
    }

    @Override
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        logger.warn("Fallback added to createOrder. Returning error message.");
        return ResponseEntity.status(503).body("The product ordering service is currently unavailable.");
    }    
}
