package com.ecommerce.order.event.stock;

import com.ecommerce.order.event.DomainEvent;

public sealed interface StockEvent extends DomainEvent
    permits StockRequested, StockReserved, StockRejected {}
