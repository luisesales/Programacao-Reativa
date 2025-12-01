package com.ecommerce.transaction.event;

import java.util.UUID;

public record TransactionRefundApproved(UUID sagaId, UUID orderId, UUID transactionId) implements TransactionEvent {}
