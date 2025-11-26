package com.ecommerce.order.event.transaction;

import com.ecommerce.order.event.DomainEvent;

public sealed interface TransactionEvent extends DomainEvent
    permits TransactionRequested, TransactionApproved, TransactionRejected {}

