package com.ecommerce.mcp.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
public class OrderResult{
    @Id    
    private String id = UUID.randomUUID().toString();
    private String response;
    private boolean success;
    @OneToOne
    @NotNull(message = "Product is mandatory")
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
    public String getId() {
        return id;
    }
} 