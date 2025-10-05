package com.ecommerce.mcp.server.model;

import java.util.HashMap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "orders")
public class Order{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)    
    private Long id;
    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")
    private String name;
    @NotEmpty(message = "Products Ordered are mandatory")
    private HashMap<Long,Integer> productsQuantity;
    @NotNull(message = "Total Price is mandatory")
    private double totalPrice;    

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public HashMap<Long, Integer> getProductsQuantity() {
        return productsQuantity;
    }
    public void setProductsQuantity(HashMap<Long, Integer> productsQuantity) {
        this.productsQuantity = productsQuantity;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void addProduct(Long productId, Integer quantity) {
        if (this.productsQuantity == null) {
            this.productsQuantity = new HashMap<>();
        }
        this.productsQuantity.put(productId, quantity);
    }
    public void removeProduct(Long productId) {
        if (this.productsQuantity != null) {
            this.productsQuantity.remove(productId);
        }
    }
}