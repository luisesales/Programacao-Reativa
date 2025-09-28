package com.ecommerce.mcp.server.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.mcp.server.exchange.OrderHttpInterface;
import com.ecommerce.mcp.server.exchange.OrderHttpInterfaceFallback;
import com.ecommerce.mcp.server.model.Order;



@Service
public class OrderAIService {

    private final OrderHttpInterface orderHttpInterface;
    private final OrderHttpInterfaceFallback fallback;

    // public OrderAIService(@Qualifier("ordersHttpInterface") OrderHttpInterface orderHttpInterface, 
    //                       @Qualifier("orderHttpInterfaceFallback") OrderHttpInterfaceFallback fallback) {
    //     this.orderHttpInterface = orderHttpInterface;
    //     this.fallback = fallback;
    // }

    public OrderAIService(OrderHttpInterface orderHttpInterface, 
                          OrderHttpInterfaceFallback fallback) {
        this.orderHttpInterface = orderHttpInterface;
        this.fallback = fallback;
    }

    public List<Order> getOrders() {
        try {
             ResponseEntity<List<Order>> response = orderHttpInterface.getAllOrders();
             return response.getBody();
        } catch (Exception e) {
            return fallback.getAllOrders().getBody();
        }
    }

    public Optional<Order> getOrder(Long orderId) {
        try {
            ResponseEntity<Order> response = orderHttpInterface.getOrderById(orderId);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            return Optional.ofNullable(fallback.getOrderById(orderId).getBody());
        }
    }

    public String createOrder(Order order) {        
        try {
            ResponseEntity<String> response = orderHttpInterface.createOrder(order);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.createOrder(order).getBody();
        }
    }
}

