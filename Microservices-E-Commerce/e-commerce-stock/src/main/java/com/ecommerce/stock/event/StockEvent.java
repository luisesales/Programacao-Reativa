package com.ecommerce.stock.event;

public sealed interface StockEvent extends DomainEvent
    permits StockRequested, StockReserved, StockRejected,
            StockIncreaseRequested, StockIncreaseReserved, StockIncreaseRejected {}
