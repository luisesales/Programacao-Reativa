package com.ecommerce.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.order.exchange.ProductHttpInterface;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepositoryReactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderServiceReactive {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceReactive.class);

    private final OrderRepositoryReactive orderRepository;
    private final ProductHttpInterface productHttpInterface;

    public OrderServiceReactive(OrderRepositoryReactive orderRepository,
                                ProductHttpInterface productHttpInterface) 
                                {
        this.orderRepository = orderRepository;
        this.productHttpInterface = productHttpInterface;        
    }

    public Flux<Order> getAllOrders() {
        logger.info("Fetching all orders (reactive)");
        return orderRepository.findAll()
                              .publishOn(Schedulers.boundedElastic())
                              .doOnError(e -> logger.error("Error fetching all orders", e));
    }

    public Mono<Order> getOrderById(Long id) {
        logger.info("Fetching order with id: {}", id);
        return orderRepository.findById(id)
                              .publishOn(Schedulers.boundedElastic())
                              .doOnError(e -> logger.error("Error fetching order id " + id, e));
    }

    public Mono<String> createOrder(Order order) {
        logger.info("Creating new order reactive: {}", order.getId());

        return productHttpInterface.orderProduct(order) 
            .publishOn(Schedulers.boundedElastic())
            .flatMap(response -> {
                if (response.getStatusCode().is2xxSuccessful()) {
                    return orderRepository.save(order)
                        .map(saved -> {
                            logger.info("Order created successfully: {}", saved.getId());
                            return "Order created successfully with id: " + saved.getId()
                                    + "\n" + response.getBody();
                        });
                } else {
                    logger.error("Failed to order products for order id: {}", order.getId());
                    String body = response.getBody() != null ? response.getBody() : "No response body";
                    return Mono.just("Failed to order products. " + body);
                }
            })
            .onErrorResume(e -> {
                logger.error("Error creating order: {}", e.getMessage(), e);
                return Mono.just("Error creating order: " + e.getMessage());
            });
    }

    public Mono<Order> updateOrder(Long id, Order orderDetails) {
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
                    return Mono.empty();
                }))
                .doOnError(e -> logger.error("Error updating order: {}", e.getMessage(), e));
    }

    public Mono<Boolean> deleteOrder(Long id) {
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
                    return Mono.just(false);
                }))                                             
                .onErrorResume(ex -> Mono.just("Fallback: Product service unavailable, cannot delete order.")
                                         .flatMap(msg -> {
                                             logger.error(msg + " Error: {}", ex.getMessage(), ex);
                                             return Mono.just(false);
                                         }));
    }
} 
    
