package com.ecommerce.mcp.server.model;

import java.util.HashMap;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order{
    @Id 
    private String id = UUID.randomUUID().toString();
    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")
    private String name;
    @NotEmpty(message = "Products Ordered are mandatory")
    @Column("products_quantity")
    private HashMap<String,Integer> productsQuantity;

    @NotNull(message = "Total Price is mandatory")
    @Column("total_price")
    private double totalPrice;    

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public HashMap<String, Integer> getProductsQuantity() {
        return productsQuantity;
    }
    public void setProductsQuantity(HashMap<String, Integer> productsQuantity) {
        this.productsQuantity = productsQuantity;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void addProduct(String productId, Integer quantity) {
        if (this.productsQuantity == null) {
            this.productsQuantity = new HashMap<>();
        }
        this.productsQuantity.put(productId, quantity);
    }
    public void removeProduct(String productId) {
        if (this.productsQuantity != null) {
            this.productsQuantity.remove(productId);
        }
    }
}