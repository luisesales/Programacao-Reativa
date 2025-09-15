package com.ecommerce.mcp.server.exchange;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.ecommerce.mcp.server.model.Order;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
@HttpExchange(url = "/orders")
public interface OrderHttpInterface {
    

    @GetExchange
    @CircuitBreaker(name= "cbOrderGetAllOrders")
    @Retry(name= "rtOrderGetAllOrders")
    @Bulkhead(name= "bhOrderGetAllOrders")    
    ResponseEntity<List<Order>> getAllOrders();

    @GetExchange("/{id}")
    @CircuitBreaker(name= "cbOrderGetOrderById")
    @Retry(name= "rtOrderGetOrderById")
    @Bulkhead(name= "bhOrderGetOrderById")  
    public ResponseEntity<Order> getOrderById(@PathVariable Long id); 

    @PostExchange
    @CircuitBreaker(name= "cbOrderCreateOrder")
    @Retry(name= "rtOrderCreateOrder")
    @Bulkhead(name= "bhOrderCreateOrder") 
    public ResponseEntity<String> createOrder(@RequestBody Order order);
}
