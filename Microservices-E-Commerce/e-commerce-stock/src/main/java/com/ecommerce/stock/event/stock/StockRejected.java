package com.ecommerce.stock.event.stock;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, String reason) implements StockEvent {}
