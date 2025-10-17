package com.ecommerce.order.exchange;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.Product;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

@HttpExchange(url = "/products")
public interface ProductHttpInterface{        

    @GetExchange
    @CircuitBreaker(name= "cbStockGetAllProducts")
    @Retry(name= "rtStockGetAllProducts")
    @Bulkhead(name= "bhStockGetAllProducts")    
    ResponseEntity<List<Product>> getAllProducts();

    @GetExchange("/{id}")
    @CircuitBreaker(name= "cbStockGetProductById")
    @Retry(name= "rtStockGetProductById")
    @Bulkhead(name= "bhStockGetProductById")  
    public ResponseEntity<Product> getProductById(@PathVariable Long id); 

    @PostExchange("/order")
    @CircuitBreaker(name= "cbStockOrderProduct")
    @Retry(name= "rtStockOrderProduct")
    @Bulkhead(name= "bhStockOrderProduct") 
    public Mono<String> orderProduct(@RequestBody Order order);


}