package com.ecommerce.transaction.event;

import java.util.UUID;

public sealed interface DomainEvent permits TransactionEvent{
    UUID sagaId();
}
