package com.ecommerce.order.event;

import java.util.List;
import java.util.UUID;

import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

public record StockIncreaseRequested(UUID sagaId, UUID orderId, List<ProductQuantityInputDTO> productsQuantity) implements StockEvent {}
