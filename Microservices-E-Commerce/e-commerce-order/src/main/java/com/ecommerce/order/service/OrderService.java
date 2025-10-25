package com.ecommerce.order.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.order.exchange.ProductHttpInterface;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderResult;
import com.ecommerce.order.model.Product;
import com.ecommerce.order.repository.OrderRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
 

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    
    private final OrderRepository orderRepository;
    private final ProductHttpInterface productHttpInterface;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public OrderService(OrderRepository orderRepository,
                        ProductHttpInterface productHttpInterface,
                        R2dbcEntityTemplate template
                        ) 
                                {
        this.orderRepository = orderRepository;
        this.productHttpInterface = productHttpInterface;    
        r2dbcEntityTemplate = template;    
    }

    public Flux<Order> getAllOrders() {
        logger.info("Fetching all orders (reactive)");
        return orderRepository.findAll()
                              .publishOn(Schedulers.boundedElastic())
                              .doOnError(e -> {
                                logger.error("Error fetching all orders", e);
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching all orders" + e.getMessage(), e));
                            });
    }

    public Mono<Order> getOrderById(UUID id) {
        logger.info("Fetching order with id: {}", id);
        return orderRepository.findById(id)
                              .publishOn(Schedulers.boundedElastic())
                              .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or access denied")))
                              .doOnError(e -> {
                                logger.error("Error fetching order id " + id, e);
                                Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is currently unavailable: " + e.getMessage(), e));
                            });
    }

    public Flux<OrderResult> createOrder(Order order) {
    logger.info("Creating new order reactive: {}", order.getId());
    if (order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
        logger.warn("Order request is empty or invalid.");
        return Flux.error(new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid order request: missing products."
        ));
    }
    logger.info("Order processed successfully for products: {}", order.getProductsQuantity());
    return productHttpInterface.orderProduct(Mono.just(order))
        .publishOn(Schedulers.boundedElastic())
        .flatMap(orderResult -> {
            if (orderResult.isSuccess()) {
                return r2dbcEntityTemplate.insert(Order.class).using(order)
                    .doOnSuccess(savedOrder ->
                        logger.info("Order created successfully: {}", savedOrder.getId())
                    )
                    .thenReturn(orderResult);
            } else {
                logger.error("Failed to order products for order id: {}", order.getId());
                return Flux.concat(
                    Flux.just(orderResult),
                    Flux.error(new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Failed to order products for order id: " + order.getId()
                    ))
                );
            }
        })
        .onErrorResume(e -> {
            logger.error("Error creating order: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException(
                "Error creating order: " + e.getMessage()
            ));
        });
}



    public Mono<Order> updateOrder(UUID id, Order orderDetails) {
        logger.info("Updating order with id: {}", id);
        return orderRepository.findById(id)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(order -> {
                    order.setName(orderDetails.getName());
                    order.setTotalPrice(orderDetails.getTotalPrice());
                    order.setProductsQuantity(orderDetails.getProductsQuantity());
                    logger.info("Order with id {} updated successfully.", id);
                    return orderRepository.save(order);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Order with id {} not found for update.", id);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with "+ id + " not found"));
                }))
                .doOnError(e -> {
                    logger.error("Error updating order: {}", e.getMessage(), e);
                    Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service is currently unavailable: " + e.getMessage(), e));
                });
    }

    public Mono<Boolean> deleteOrder(UUID id) {
        logger.info("Deleting order with id: {}", id);
        return orderRepository.findById(id)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(order ->
                    orderRepository.delete(order)
                                   .then(Mono.fromCallable(() -> {
                                       logger.info("Order with id {} deleted successfully.", id);
                                       return true;
                                   }))
                )
                .switchIfEmpty(Mono.defer(() -> {
                    logger.warn("Order with id {} not found for deletion.", id);
                    return Mono.just(false)
                                .flatMap(msg -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order with "+ id + " not found")));
                }))                                             
                .onErrorResume(ex -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service unavailable, cannot delete order with id"+ id))
                                         .flatMap(msg -> {
                                             logger.error(msg + " Error: {}", ex.getMessage(), ex);
                                             return Mono.just(false);
                                         }));
    }

    public Flux<Product> getProducts() {
        logger.info("Fetching products from Product Service");
        return productHttpInterface.getAllProducts().onErrorResume(e -> {
            logger.error("Error Returning Products from Product Service: {}", e.getMessage(), e);
            return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error Returning Products Service is not available", e));
        });   
    }
} 
    
