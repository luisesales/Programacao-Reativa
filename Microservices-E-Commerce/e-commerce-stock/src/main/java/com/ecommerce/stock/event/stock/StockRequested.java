package com.ecommerce.stock.event.stock;

import java.util.UUID;

public record StockRequested(UUID sagaId, UUID orderId) implements StockEvent {}
