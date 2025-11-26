package com.ecommerce.order.event.order;

import java.util.List;
import java.util.UUID;

import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

public record OrderCreated(UUID sagaId, UUID orderId, String name, Double totalPrice, List<ProductQuantityInputDTO> products) implements OrderEvent  { }

