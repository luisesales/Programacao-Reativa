package com.ecommerce.order.model.saga;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("saga_context")
public class SagaContext {

    @Id
    @Column("id")
    private UUID id;

    @Column("name")
    private String name;

    @Column("total_price")
    private double totalPrice;

    @Column("transaction_id")
    private UUID transactionId;

    @Column("stock_reservation_id")
    private UUID stockReservationId;

    @Column("saga_id")
    private UUID sagaId;

    public SagaContext(String name, double totalPrice, UUID transactionId, UUID stockReservationId, UUID sagaId){
        this.name = name;
        this.totalPrice = totalPrice;
        this.transactionId = transactionId;
        this.stockReservationId = stockReservationId;        
        this.sagaId = sagaId;
    }

    public SagaContext(UUID sagaId){
        this.sagaId = sagaId;
        this.totalPrice = 0.0;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public void setTransactionId(UUID transactionId){
        this.transactionId = transactionId;
    }
    public void setStockReservationId(UUID stockReservationId){
        this.stockReservationId = stockReservationId;
    }    
    public void setSagaId(UUID sagaId){
        this.sagaId = sagaId;
    }

    public UUID getId(){ return id; }
    public String getName(){ return name; }
    public double getTotalPrice() { return totalPrice; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getStockReservationId() { return stockReservationId; }    
    public UUID getSagaId() { return sagaId; }
}
