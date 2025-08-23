package com.ecommerce.mcpserver.exchange;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.ecommerce.mcpserver.model.Order;
import com.ecommerce.order.model.Order;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
@HttpExchange(url = "/orders")
public interface OrderHttpInterface {
    

    @GetExchange
    @CircuitBreaker(name= "cbOrderGetAllOrders")
    @Retry(name= "rtStockGetAllOrders")
    @Bulkhead(name= "bhStockGetAllOrders")    
    ResponseEntity<List<Order>> getAllOrders();

    @GetExchange("/{id}")
    @CircuitBreaker(name= "cbStockGetOrderById")
    @Retry(name= "rtStockGetOrderById")
    @Bulkhead(name= "bhStockGetOrderById")  
    public ResponseEntity<Order> getOrderById(@PathVariable Long id); 

    @PostExchange
    @CircuitBreaker(name= "cbStockOrderOrder")
    @Retry(name= "rtStockOrderOrder")
    @Bulkhead(name= "bhStockGetOrderById") 
    public ResponseEntity<String> createOrder(@RequestBody Order order);
}
