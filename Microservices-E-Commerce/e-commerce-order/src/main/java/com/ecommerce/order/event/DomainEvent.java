package com.ecommerce.order.event;

import java.util.UUID;

import com.ecommerce.order.event.order.OrderEvent;
import com.ecommerce.order.event.transaction.TransactionEvent;
import com.ecommerce.order.event.stock.StockEvent;

public sealed interface DomainEvent permits OrderEvent, TransactionEvent, StockEvent {
    UUID sagaId();
}
