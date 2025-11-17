package com.ecommerce.transaction.model.dto;

import java.util.UUID;

public record ProductQuantityInputDTO(
        UUID productId,
        Integer quantity
) {}