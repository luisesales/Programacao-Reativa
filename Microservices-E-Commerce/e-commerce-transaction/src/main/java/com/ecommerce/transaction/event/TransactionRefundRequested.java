package com.ecommerce.transaction.event;

import java.util.UUID;

public record TransactionRefundRequested(UUID sagaId, UUID orderId, String name, double totalPrice) implements TransactionEvent {}
