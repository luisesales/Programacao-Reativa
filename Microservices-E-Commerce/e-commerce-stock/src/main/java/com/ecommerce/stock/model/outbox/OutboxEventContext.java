package com.ecommerce.stock.model.outbox;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

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

    @Column("order_id")
    private UUID orderId;

    public OutboxEventContext(UUID outboxEventId, String fieldName, String fieldValue, UUID orderId) {        
        this.outboxEventId = outboxEventId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.orderId = orderId;
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

    public UUID getOrderId() {
        return orderId;
    }
    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

}
