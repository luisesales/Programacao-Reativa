package com.ecommerce.order.model.dto;

import java.util.List;
import java.util.UUID;

public record OrderCreatedEventDTO(UUID sagaId, UUID orderId, Double totalPrice, List<ProductQuantityInputDTO> products) { }

