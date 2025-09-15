package com.ecommerce.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
        Optional<Order> findByName(String name);
        Optional<Order> findById(Long id);
        List<Order> findByTotalPriceBetween(Double minPrice, Double maxPrice);
        List<Order> findAll();
        Order save(Order order);
        void delete(Order order);
        void deleteById(Long id);
        //List<Order> findByProductsQuantity(Long productId);        
    }