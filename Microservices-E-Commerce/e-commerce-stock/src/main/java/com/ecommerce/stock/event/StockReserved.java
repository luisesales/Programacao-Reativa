package com.ecommerce.stock.event;

import java.util.List;
import java.util.UUID;

import com.ecommerce.stock.model.dto.ProductQuantityInputDTO;

public record StockReserved(UUID sagaId, UUID orderId, List<ProductQuantityInputDTO> productsQuantity) implements StockEvent {}
