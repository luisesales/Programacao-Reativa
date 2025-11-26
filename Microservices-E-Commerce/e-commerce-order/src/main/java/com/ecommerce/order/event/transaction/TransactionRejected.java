package com.ecommerce.order.event.transaction;

import java.util.UUID;

public record TransactionRejected(UUID sagaId, UUID orderId, String reason) implements TransactionEvent {}
