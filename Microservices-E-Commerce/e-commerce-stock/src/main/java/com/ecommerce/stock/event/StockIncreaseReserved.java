package com.ecommerce.stock.event;

import java.util.UUID;

public record StockIncreaseReserved(UUID sagaId, UUID orderId, UUID productId, Integer quantity) implements StockEvent {}
