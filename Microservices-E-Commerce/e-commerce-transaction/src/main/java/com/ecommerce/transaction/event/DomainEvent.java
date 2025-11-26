package com.ecommerce.transaction.event;

import java.util.UUID;

import com.ecommerce.transaction.event.transaction.TransactionEvent;

public sealed interface DomainEvent permits TransactionEvent{
    UUID sagaId();
}
