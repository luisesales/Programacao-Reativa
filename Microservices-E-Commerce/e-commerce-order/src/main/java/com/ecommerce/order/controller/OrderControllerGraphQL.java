package com.ecommerce.order.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderResult;
import com.ecommerce.order.model.Product;
import com.ecommerce.order.model.dto.OrderInputDTO;
import com.ecommerce.order.model.dto.OrderDTO;
import com.ecommerce.order.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Controller
public class OrderControllerGraphQL {
    @Autowired
    private final OrderService orderService;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    public OrderControllerGraphQL(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public Flux<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @QueryMapping
    public Mono<OrderDTO> getOrderById(@Argument UUID id) {
        return orderService.getOrderById(id);
                                
    }

    @QueryMapping
    public Flux<Product> getProducts() {
        return orderService.getProducts();
    }

    
    @QueryMapping
    public Flux<Product> getProductsByCategory(@Argument String category) {
        return orderService.getProductsByCategory();
    }

    @MutationMapping
    public Flux<OrderResult> createOrder(@Argument OrderInputDTO order) {        
        logger.info("Request received to order products: {} with price: {}",order.name(), order.totalPrice());            
        return orderService.createOrderDTO(order);        
    }
}
