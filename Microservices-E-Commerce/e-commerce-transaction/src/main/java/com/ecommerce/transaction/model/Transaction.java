package com.ecommerce.transaction.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Table(name = "transactions")
public class Transaction {
    @Id 
    private UUID id;

    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")
    private String name;

    @Column("order_id")
    private UUID orderId;

    @NotNull(message = "Total Price is mandatory")
    @Column("total_price")
    private double totalPrice;   
    
    public Transaction() {
        this.id = null;
        this.name = "N/A";
        this.orderId = null;        
        this.totalPrice = 0.0;
    }

    public Transaction(String name, double totalPrice, UUID orderId) {
        this.name = name;        
        this.totalPrice = totalPrice;
        this.orderId = orderId;
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
    public UUID getOrderId() {
        return orderId;
    }
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
