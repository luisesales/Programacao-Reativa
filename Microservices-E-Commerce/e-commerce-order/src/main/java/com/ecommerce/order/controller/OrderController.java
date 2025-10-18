package com.ecommerce.order.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderResult;
import com.ecommerce.order.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private final OrderService orderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderController(OrderService orderService) {
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
    public Flux<OrderResult> createOrder(@RequestBody Mono<Order> orderMono) {
        return orderMono
        .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
        .flatMapMany(order -> {
            logger.info("Request received to order products: {} with price: {}",
            order.getName(), order.getTotalPrice());
            if (order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
                    logger.warn("Order request is empty or invalid.");
                    return Flux.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid order request: missing products."
                    ));
            }
            logger.info("Order processed successfully for products: {}", order.getProductsQuantity());
            return orderService.createOrder(order);
        })
        .onErrorResume(e -> {
            logger.error("Error creating order: {}", e.getMessage());
            OrderResult errorResult = new OrderResult();
            errorResult.setSuccess(false);
            errorResult.setResponse("Error creating order: " + e.getMessage());
            return Flux.just(errorResult);            
        });
    }
}
