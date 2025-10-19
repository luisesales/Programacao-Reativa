package com.ecommerce.stock.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;


import jakarta.validation.constraints.NotNull;


import java.util.UUID;

public class OrderResult{
    @Id
    private String id = UUID.randomUUID().toString();
    private String response;
    private boolean success;
    @Transient
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