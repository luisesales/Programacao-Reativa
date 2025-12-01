package com.ecommerce.transaction.event;

import java.util.UUID;

public record TransactionRequested(UUID sagaId, UUID orderId, String name, double totalPrice) implements TransactionEvent {}
