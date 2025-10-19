package com.ecommerce.mcp.server.service;

import java.util.Flux;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.mcp.server.exchange.OrderHttpInterface;
import com.ecommerce.mcp.server.model.Order;



@Service
public class OrderAIService {

    private final OrderHttpInterface orderHttpInterface;;

    public OrderAIService(OrderHttpInterface orderHttpInterface 
                          ) {
        this.orderHttpInterface = orderHttpInterface;
    }

    public Flux<Order> getOrders() {
        try {
             Flux<Order> response = orderHttpInterface.getAllOrders();
             return response.getBody();
        } catch (Exception e) {
            return fallback.getAllOrders().getBody();
        }
    }

    public Optional<Order> getOrder(String orderId) {
        try {
            ResponseEntity<Order> response = orderHttpInterface.getOrderById(orderId);
            return Optional.ofNullable(response.getBody());
        } catch (Exception e) {
            return Optional.ofNullable(fallback.getOrderById(orderId).getBody());
        }
    }

    public Flux<OrderResult> createOrder(Order order) {        
        try {
            Flux<OrderResult> response = orderHttpInterface.createOrder(order);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return fallback.createOrder(order).getBody();
        }
    }
}

