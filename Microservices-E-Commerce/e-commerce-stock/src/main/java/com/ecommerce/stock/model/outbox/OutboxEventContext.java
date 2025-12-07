package com.ecommerce.stock.model.outbox;

import java.util.UUID;

import com.ecommerce.stock.event.StockReserved;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.Order;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.ecommerce.stock.event.DomainEvent;
import com.ecommerce.stock.event.StockRequested;
import com.ecommerce.stock.event.StockRejected;
import com.ecommerce.stock.event.StockReserved;
import com.ecommerce.stock.event.StockIncreaseRequested;
import com.ecommerce.stock.event.StockIncreaseApproved;
import com.ecommerce.stock.event.StockIncreaseRejected;




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

    public DomainEvent toDomainEvent(String eventType,Order tx) {
        return switch (eventType) {

            case "StockRequested" -> new StockRequested(
                getOutboxEventId(),
                tx.getOrderId(),
                tx.getName(),
                tx.getTotalPrice(),                     
            );

            case "StockApproved" -> new StockReserved(
                getOutboxEventId(),                
                tx.getId(),
            );

            case "StockRejected" -> new StockRejected(
                getOutboxEventId(),
                tx.getId(),
                getReason()        
            );

            case "StockIncreaseRequested" -> new StockIncreaseRequested(
                getOutboxEventId(),
                tx.getOrderId(),
                tx.getName(),
                tx.getTotalPrice(),                     
            );

            case "StockIncreaseApproved" -> new StockIncreaseApproved(
                getOutboxEventId(),
                tx.getOrderId(),
                tx.getId()
            );

            case "StockIncreaseRejected" -> new StockIncreaseRejected(
                getOutboxEventId(),
                tx.getOrderId(),                
                getReason()
            );
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
    private String getReason() {
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
