package com.ecommerce.order.httpInterface;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecommerce.order.model.Product;

@Component
@HttpExchange(url = "/products")
public interface ProductHttpInterface{    
    private static final Logger logger = LoggerFactory.getLogger(ProductHttpInterface.class);

    @GetExchange
    ResponseEntity<List<Product>> getAllProducts();

    @GetExchange("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id); 


}