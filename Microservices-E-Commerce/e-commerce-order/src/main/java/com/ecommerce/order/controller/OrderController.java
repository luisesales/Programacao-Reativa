package com.ecommerce.order.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.Product;
import com.ecommerce.order.model.dto.OrderDTO;
import com.ecommerce.order.model.dto.OrderInputDTO;
import com.ecommerce.order.model.saga.OrderResultSaga;
import com.ecommerce.order.model.saga.SagaContextProductsQuantity;
import com.ecommerce.order.service.OrderService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/orders")
public class OrderController {

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
    public Mono<OrderDTO> getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id);
                                
    }

    @GetMapping(path = "/products", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getProducts() {
        return orderService.getProducts();
    }

    
    @GetMapping(path = "/products/category", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getProductsByCategory(@RequestParam String category) {
        return orderService.getProductsByCategory(category);
    }

    /*@PostMapping
    public Flux<OrderResult> createOrder(@RequestBody Mono<Order> orderMono) {
        return orderMono
        .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
        .flatMapMany(order -> {
            logger.info("Request received to order products: {} with price: {}",order.getName(), order.getTotalPrice());            
            return orderService.createOrder(order);
        });        
    }*/

    @GetMapping(path = "/{id}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<SagaContextProductsQuantity> getOrderStatus(@PathVariable UUID orderId) {
        return orderService.getOrderStatus(orderId);
    }
    
    
   @PostMapping
   public Mono<ResponseEntity<OrderResultSaga>> createOrder(@RequestBody Mono<OrderInputDTO> orderMono){
    
    return orderMono
        .flatMap(order -> orderService.createOrder(order))
        .map(orderResult ->
            ResponseEntity
                .accepted()
                .body(orderResult)
        );
}
}
