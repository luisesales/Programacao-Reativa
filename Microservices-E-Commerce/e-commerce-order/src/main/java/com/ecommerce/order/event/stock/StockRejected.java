package com.ecommerce.order.event.stock;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, String reason) implements StockEvent {}
