package com.ecommerce.order.event;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, String reason) implements StockEvent {}
