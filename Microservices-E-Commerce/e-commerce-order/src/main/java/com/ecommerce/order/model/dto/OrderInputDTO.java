package com.ecommerce.order.model.dto;

import java.util.List;
import java.util.UUID;

public record OrderInputDTO(        
        String name,
        List<ProductQuantityInputDTO> productsQuantity,
        Double totalPrice
)  {}
