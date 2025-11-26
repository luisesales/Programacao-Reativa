package com.ecommerce.transaction.event.transaction;

import com.ecommerce.transaction.event.DomainEvent;

public sealed interface TransactionEvent extends DomainEvent
    permits TransactionRequested, TransactionApproved, TransactionRejected {}

