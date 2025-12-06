package com.ecommerce.stock.event;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, UUID productId, String reason) implements StockEvent {}
