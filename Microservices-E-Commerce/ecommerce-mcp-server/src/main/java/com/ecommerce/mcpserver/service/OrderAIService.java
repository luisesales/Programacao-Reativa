package com.ecommerce.mcpserver.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;

import com.ecommerce.mcpserver.exchange.OrderHttpInterface;
import com.ecommerce.mcpserver.exchange.OrderHttpInterfaceFallback;
import com.ecommerce.mcpserver.model.Order;



@Service
public class OrderAIService {

    private final OrderHttpInterface orderHttpInterface;
    private final OrderHttpInterfaceFallback fallback;

    public OrderAIService(@Qualifier("orderHttpInterface") OrderHttpInterface orderHttpInterface, OrderHttpInterfaceFallback fallback) {
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
            return fallback.getOrderById(orderId).getBody();
        }
    }

    public Order createOrder(Order order) {        
        try {
            ResponseEntity<String> response = orderHttpInterface.createOrder(order);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.createOrder(order).getBody();
        }
    }
}

