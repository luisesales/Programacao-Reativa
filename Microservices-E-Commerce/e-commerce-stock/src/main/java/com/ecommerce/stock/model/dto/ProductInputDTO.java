package com.ecommerce.stock.model.dto;

public record ProductInputDTO(
    String name,
    String description,
    Double price,
    String category,
    Integer stockQuantity
) {}
