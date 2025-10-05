package com.ecommerce.order.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ecommerce.order.exchange.ProductHttpInterface;
import com.ecommerce.order.exchange.ProductHttpInterfaceFallback;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    private final ProductHttpInterface productHttpInterface;
    private final ProductHttpInterfaceFallback fallback;

    public OrderService(ProductHttpInterface productHttpInterface,
                            ProductHttpInterfaceFallback fallback) {
        this.productHttpInterface = productHttpInterface;
        this.fallback = fallback;
    }

    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepository.findAll();
    }
    public Optional<Order> getOrderById(Long id) {
        logger.info("Fetching order with id: {}", id);
        return orderRepository.findById(id);
        
    }

    public String createOrder(Order order) {
        logger.info("Creating new order: {}", order.getId());
        try {

            ResponseEntity<String> response = productHttpInterface.orderProduct(order);

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                Order savedOrder = orderRepository.save(order);
                logger.info("Order created successfully: {}", savedOrder.getId());
                return "Order created successfully with id: " + savedOrder.getId() + "\n" + response.getBody();
            } else {
                logger.error("Failed to order products for order id: {}", order.getId());
                return "Failed to order products." + (response != null ? response.getBody() : "No response from product service");
            }
        } catch (Exception e) {
            logger.error("Error creating order: {}", e.getMessage());
            return "Error creating order: " + e.getMessage();
        }
    }

    // public Order createOrder(Order order) {
    //     logger.info("Creating new order: {}", order.getName());
    //     return orderRepository.save(order);
    // }
    
    public Order updateOrder(Long id, Order orderDetails) {
        logger.info("Updating order with id: {}", id);
        return orderRepository.findById(id)
                .map(order -> {
                    order.setName(orderDetails.getName());                    
                    order.setTotalPrice(orderDetails.getTotalPrice());
                    order.setProductsQuantity(orderDetails.getProductsQuantity());                                        
                    logger.info("Order with id {} updated successfully.", id);                    
                    return orderRepository.save(order);                    
                })
                .orElseGet(() -> {
                    logger.warn("Order with id {} not found for update.", id);
                    return null;
                });
    }
    public boolean deleteOrder(Long id) {
        logger.info("Deleting order with id: {}", id);
        orderRepository.findById(id)
                .ifPresent( order -> {
                    logger.info("Order with id {} found for deletion.", id);
                    orderRepository.delete(order);
                    logger.info("Order with id {} deleted successfully.", id);                    
                });
        logger.warn("Order with id {} not found for deletion.", id);
        return false;
    }
}
