package com.ecommerce.order.event;

import java.util.UUID;

public record StockRequested(UUID sagaId, UUID orderId) implements StockEvent {}
