package com.ecommerce.mcp.server.service;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.mcp.server.exchange.OrderHttpInterface;
import com.ecommerce.mcp.server.model.Order;
import com.ecommerce.mcp.server.model.OrderResult;



@Service
public class OrderAIService {

    private final OrderHttpInterface orderHttpInterface;;

    public OrderAIService(OrderHttpInterface orderHttpInterface 
                          ) {
        this.orderHttpInterface = orderHttpInterface;
    }

    public Flux<Order> getOrders() {
        return orderHttpInterface.getAllOrders().onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Returning Orders", e));
        });        
    }

    public Mono<Order> getOrder(String orderId) {
        return orderHttpInterface.getOrderById(orderId).onErrorResume(e -> {
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Returning Order with "+ orderId , e));
        });
    }

    public Flux<OrderResult> createOrder(Order order) {            
        return orderHttpInterface.createOrder(Mono.just(order)).onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Order " +  order.getName(), e));
        });
    }
}