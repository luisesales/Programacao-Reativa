package com.ecommerce.order.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

public class OrderResult{
    @Id    
    private UUID id;
    private String response;
    private boolean success;
    @Transient
    private Product product;

    public OrderResult(boolean success, String response, Product product) {
        this.success = success;
        this.response = response;
        this.product = product;
    }

    public void setResponse(String response) {
        this.response = response;
    }
    public String getResponse() {
        return response;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
    public Product getProduct() {
        return product;
    }
    public UUID getId() {
        return id;
    }
} 