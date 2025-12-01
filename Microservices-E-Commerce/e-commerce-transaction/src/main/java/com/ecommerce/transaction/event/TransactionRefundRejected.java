package com.ecommerce.transaction.event;

import java.util.UUID;

public record TransactionRefundRejected(UUID sagaId, UUID orderId, String reason) implements TransactionEvent {}
