package com.ecommerce.order.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;

import com.ecommerce.order.exchange.ProductHttpInterface;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductHttpInterface productHttpInterface;

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
            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully with id: {}", savedOrder.getId());
            return productHttpInterface.orderProduct(savedOrder).getBody();
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
