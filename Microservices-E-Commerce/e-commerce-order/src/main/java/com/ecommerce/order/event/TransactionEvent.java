package com.ecommerce.order.event;

public sealed interface TransactionEvent extends DomainEvent
    permits TransactionRequested, TransactionApproved, TransactionRejected, 
    TransactionRefundRequested, TransactionRefundApproved, TransactionRefundRejected {}

