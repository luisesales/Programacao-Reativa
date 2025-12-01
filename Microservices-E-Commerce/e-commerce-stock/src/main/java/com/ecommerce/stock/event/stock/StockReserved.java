package com.ecommerce.stock.event.stock;

import java.util.UUID;

public record StockReserved(UUID sagaId, UUID orderId) implements StockEvent {}
