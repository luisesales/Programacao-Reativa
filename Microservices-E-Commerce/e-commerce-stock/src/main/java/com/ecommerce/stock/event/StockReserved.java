package com.ecommerce.stock.event;

import java.util.UUID;

public record StockReserved(UUID sagaId, UUID orderId, UUID productId, Integer quantity) implements StockEvent {}
