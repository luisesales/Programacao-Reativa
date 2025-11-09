package com.ecommerce.order.model;

import java.util.HashMap;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Table(name = "orders")
public class Order{
    @Id 
    private UUID id;
    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")
    private String name;
    @Transient
    private HashMap<UUID,Integer> productsQuantity;

    @NotNull(message = "Total Price is mandatory")
    @Column("total_price")
    private double totalPrice;   
    
    public Order() {
        this.id = null;
        this.name = "N/A";
        this.productsQuantity = new HashMap<>();
        this.totalPrice = 0.0;
    }

    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public HashMap<UUID, Integer> getProductsQuantity() {
        return productsQuantity;
    }
    public void setProductsQuantity(HashMap<UUID, Integer> productsQuantity) {
        this.productsQuantity = productsQuantity;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void addProduct(UUID productId, Integer quantity) {
        if (this.productsQuantity == null) {
            this.productsQuantity = new HashMap<>();
        }
        this.productsQuantity.put(productId, quantity);
    }
    public void removeProduct(UUID productId) {
        if (this.productsQuantity != null) {
            this.productsQuantity.remove(productId);
        }
    }
}