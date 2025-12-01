package com.ecommerce.stock.event;

import java.util.UUID;

import com.ecommerce.stock.event.stock.OrderEvent;
import com.ecommerce.stock.event.transaction.TransactionEvent;
import com.ecommerce.stock.event.stock.StockEvent;

public sealed interface DomainEvent permits StockEvent {
    UUID sagaId();
}
