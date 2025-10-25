package com.ecommerce.mcp.server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.util.UUID;


public class OrderResult{
    @Id    
    private UUID id;
    private String response;
    private boolean success;

    @Transient
    private Product product;

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