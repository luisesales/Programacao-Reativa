package com.ecommerce.transaction.event.transaction;

import java.util.UUID;

public record TransactionApproved(UUID sagaId, UUID orderId, UUID transactionId) implements TransactionEvent {}
