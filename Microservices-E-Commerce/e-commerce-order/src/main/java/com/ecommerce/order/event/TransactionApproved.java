package com.ecommerce.order.event;

import java.util.UUID;

public record TransactionApproved(UUID sagaId, UUID orderId, UUID transactionId) implements TransactionEvent {}
