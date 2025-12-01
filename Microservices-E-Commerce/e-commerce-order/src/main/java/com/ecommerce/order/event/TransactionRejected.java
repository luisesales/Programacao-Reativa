package com.ecommerce.order.event;

import java.util.UUID;

public record TransactionRejected(UUID sagaId, UUID orderId, String reason) implements TransactionEvent {}
