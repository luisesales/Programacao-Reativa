package com.ecommerce.stock.event;

import java.util.UUID;

public record StockRejected(UUID sagaId, UUID orderId, UUID productId, Integer quantity,Double totalPrice, String reason) implements StockEvent {}
