package com.ecommerce.mcpserver.model;

import java.util.HashMap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Order{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private HashMap<Long,Integer> productsQuantity;
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