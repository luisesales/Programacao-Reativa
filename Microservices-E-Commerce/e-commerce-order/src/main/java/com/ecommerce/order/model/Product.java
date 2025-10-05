package com.ecommerce.order.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")    
    private String name;
    @NotBlank(message = "Description is mandatory")
    @Size(min = 5, max=30, message = "The description must have at least 5 characters and maximum 30 characters")
    private String description;
    @NotNull(message = "Price is mandatory")
    private double price;
    @NotNull(message = "Stock Quantity is mandatory")
    private int stockQuantity;
    @NotBlank(message = "Category is mandatory")
    @Size(min = 5, max=15, message = "The category must have at least 5 characters and maximum 15 characters")
    private String category;

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public double getPrice() {
        return price;
    }
    public int getStockQuantity() {
        return stockQuantity;
    }
    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
    public void decreaseStock(int quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock to decrease by " + quantity);
        }
    }

}
