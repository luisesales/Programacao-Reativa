package com.ecommerce.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.ecommerce.order.service.OrderServiceReactive;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.ecommerce.order.model.Order;


@RestController
@RequestMapping("/orders")
public class OrderControllerReactive {
    @Autowired
    private final OrderServiceReactive orderService;

    public OrderControllerReactive(OrderServiceReactive orderService) {
        this.orderService = orderService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or access denied")));
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<String> createOrder(@RequestBody Mono<Order> orderMono) {
        return orderMono.flatMap(order -> orderService.createOrder(order))
            .then(Mono.just("Order created successfully"))
            .onErrorResume(e -> Mono.just("Error creating order: " + e.getMessage()));
    }
}
