package com.ecommerce.mcp.server.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.mcp.server.exchange.OrderHttpInterface;
import com.ecommerce.mcp.server.model.Order;
import com.ecommerce.mcp.server.model.OrderResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;



@Service
public class OrderAIService {

    private final OrderHttpInterface orderHttpInterface;;

    public OrderAIService(OrderHttpInterface orderHttpInterface 
                          ) {
        this.orderHttpInterface = orderHttpInterface;
    }

    public Flux<Order> getOrders() {
        return orderHttpInterface.getAllOrders()            
            .onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Returning Orders", e));
        }).subscribeOn(Schedulers.boundedElastic());        
    }

    public Mono<Order> getOrder(String orderId) {
        return orderHttpInterface.getOrderById(orderId)
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Returning Order with "+ orderId , e));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<OrderResult> createOrder(Order order) {            
        return orderHttpInterface.createOrder(Mono.just(order))
            .subscribeOn(Schedulers.boundedElastic())
            .onErrorResume(e -> {
            return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Creating Order " +  order.getName(), e));
        }).subscribeOn(Schedulers.boundedElastic());
    }
}