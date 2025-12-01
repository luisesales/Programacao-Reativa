package com.ecommerce.stock.event.stock;

import com.ecommerce.stock.event.DomainEvent;

public sealed interface StockEvent extends DomainEvent
    permits StockRequested, StockReserved, StockRejected {}
