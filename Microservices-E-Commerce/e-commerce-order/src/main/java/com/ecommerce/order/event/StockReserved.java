package com.ecommerce.order.event;

import java.util.UUID;

public record StockReserved(UUID sagaId, UUID orderId) implements StockEvent {}
