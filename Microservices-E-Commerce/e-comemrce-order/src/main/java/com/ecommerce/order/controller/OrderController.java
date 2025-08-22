package com.ecommerce.order.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.service.OrderService;

@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAllOrders() {
        logger.info("Request received to get all orders.");
        return orderService.getAllOrders();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        logger.info("Request received to get order with id: {}", id);
        Optional<Order> order = orderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        logger.info("Request received to create new order: {}", order.getId());
        String orderResult = orderService.createOrder(order);
        logger.info("Order sent successfully with results: {}", orderResult);
        return ResponseEntity.ok(orderResult);                
    }

    // @PostMapping
    // public ResponseEntity<Order> createOrder(@RequestBody Order order) {
    //     logger.info("Request received to create new order: {}", order.getId());
    //     Order createdOrder = orderService.createOrder(order);
    //     logger.info("Order created successfully: {}", createdOrder.getId());
    //     return ResponseEntity.ok(createdOrder);                
    // }
    // @PutMapping("/{id}")
    // public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
    //     logger.info("Request received to update order with id: {}", id);
    //     return orderService.getOrderById(id)
    //             .map(order -> {
    //                 Order updatedOrder = orderService.updateOrder(id, order);
    //                 return ResponseEntity.ok(updatedOrder);
    //             })
    //             .orElseGet(() -> ResponseEntity.notFound().build());
    // }
    // @DeleteMapping("/{id}")
    // public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
    //     logger.info("Request received to delete order with id: {}", id);
    //     if(orderService.deleteOrder(id)){
    //         logger.info("Order with id {} deleted successfully.", id);
    //         return ResponseEntity.ok("Order deleted successfully.");
    //     } else{
    //         logger.warn("Order with id {} not found for deletion.", id);
    //         return ResponseEntity.notFound().build();
    //     }
    // }    
}
