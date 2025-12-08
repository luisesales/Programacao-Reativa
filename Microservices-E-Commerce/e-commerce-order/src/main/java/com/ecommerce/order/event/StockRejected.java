package com.ecommerce.order.event;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, UUID productId, Integer quantity, String reason) implements StockEvent {}
