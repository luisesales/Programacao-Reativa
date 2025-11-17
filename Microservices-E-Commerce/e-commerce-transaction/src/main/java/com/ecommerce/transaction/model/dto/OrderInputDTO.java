package com.ecommerce.transaction.model.dto;

import java.util.List;
import java.util.UUID;

public record OrderInputDTO(
        UUID id,
        String name,
        List<ProductQuantityInputDTO> productsQuantity,
        Double totalPrice
)  {}
