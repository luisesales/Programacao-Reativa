package com.ecommerce.stock.event;

import java.util.UUID;

public sealed interface DomainEvent permits StockEvent {
    UUID sagaId();
}
