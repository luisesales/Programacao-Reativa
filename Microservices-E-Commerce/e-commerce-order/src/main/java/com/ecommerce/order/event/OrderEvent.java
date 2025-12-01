package com.ecommerce.order.event;

public sealed interface OrderEvent extends DomainEvent 
  permits OrderCreated, OrderCompleted, OrderCancelled {}