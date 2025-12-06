package com.ecommerce.stock.event;

import java.util.UUID;

public record StockIncreaseRejected(UUID sagaId, UUID orderId, UUID productId, String reason) implements StockEvent {}
