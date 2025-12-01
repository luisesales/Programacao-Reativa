package com.ecommerce.order.event;

import java.util.UUID;

public sealed interface DomainEvent permits OrderEvent, TransactionEvent, StockEvent {
    UUID sagaId();
}
