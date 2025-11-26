package com.ecommerce.order.event.order;

import com.ecommerce.order.event.DomainEvent;

public sealed interface OrderEvent extends DomainEvent 
  permits OrderCreated, OrderCompleted, OrderCancelled {}