package com.ecommerce.transaction.model.outbox;

import java.util.UUID;

import com.ecommerce.transaction.model.Transaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.ecommerce.transaction.event.DomainEvent;
import com.ecommerce.transaction.event.TransactionApproved;
import com.ecommerce.transaction.event.TransactionRefundApproved;
import com.ecommerce.transaction.event.TransactionRefundRejected;
import com.ecommerce.transaction.event.TransactionRefundRequested;
import com.ecommerce.transaction.event.TransactionRejected;

@Table("outbox_event_context")
public class OutboxEventContext {
    @Id
    @Column("id")
    private UUID id;

    @Column("outbox_event_id")
    private UUID outboxEventId;

    @Column("field_name")
    private String fieldName;

    @Column("field_value")
    private String fieldValue;

    @Column("transaction_id")
    private UUID transactionId;

    public OutboxEventContext(UUID outboxEventId, String fieldName, String fieldValue, UUID transactionId) {        
        this.outboxEventId = outboxEventId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.transactionId = transactionId;
    }   

    // Getters and Setters
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public UUID getOutboxEventId() {
        return outboxEventId;
    }
    public void setOutboxEventId(UUID outboxEventId) {
        this.outboxEventId = outboxEventId;
    }
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public String getFieldValue() {
        return fieldValue;
    }
    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getReason() {
        if ("reason".equals(this.fieldName)) {
            return this.fieldValue;
        }
        return null;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

}
