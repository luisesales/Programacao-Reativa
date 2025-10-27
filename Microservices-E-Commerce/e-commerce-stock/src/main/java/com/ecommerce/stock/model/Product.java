package com.ecommerce.stock.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Table(name = "product")
public class Product {
    @Id
    private UUID id;
    @NotBlank(message = "Name is mandatory")
    @Size(min = 5, message = "The name must have at least 5 characters")    
    private String name;
    @NotBlank(message = "Description is mandatory")
    @Size(min = 5, max = 100, message = "The description must have at least 5 characters and maximum 100 characters")
    private String description;
    @NotNull(message = "Price is mandatory")
    private double price;
    @NotNull(message = "Stock Quantity is mandatory")
    @Column("stock_quantity")
    private Integer stockQuantity;
    @NotBlank(message = "Category is mandatory")
    @Size(min = 5, max = 15, message = "The category must have at least 5 characters and maximum 15 characters")

    public Product() {
        this.id = null;
        this.name = "N/A";
        this.description = "N/A";
        this.price = 0.0;
        this.stockQuantity = 0; 
        this.category = "N/A";
    }
    
    private String category;

    public UUID getId() {
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

    public void setNotFound() {
        this.id = null;
        this.name = "Product Not Found";
        this.description = "N/A";
        this.price = 0.0;
        this.stockQuantity = 0;
        this.category = "N/A";
    }

}
