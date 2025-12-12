package com.ecommerce.order.model.saga;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

import jakarta.validation.constraints.NotBlank;

@Table("saga_context")
public class SagaContext {

    @Id
    @Column("id")
    private UUID id;

    @NotBlank(message="Order ID is necessary")
    @Column("order_id")
    private UUID orderId;

    @Column("name")
    private String name;

    @Column("total_price")
    private double totalPrice;

    @Column("transaction_id")
    private UUID transactionId;

    @Column("saga_id")
    private UUID sagaId;

    @Transient
    private List<ProductQuantityInputDTO> productsQuantity;

    public SagaContext(UUID orderId,String name, double totalPrice, UUID transactionId, UUID sagaId){
        this.orderId = orderId;
        this.name = name;
        this.totalPrice = totalPrice;
        this.transactionId = transactionId;             
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
    public void setSagaId(UUID sagaId){
        this.sagaId = sagaId;
    }
    public void setProductsQuantity(List<ProductQuantityInputDTO> productsQuantity){
        this.productsQuantity = productsQuantity;
    }
    public void setOrderId(UUID orderId) { 
        this.orderId = orderId; 
    }

    public UUID getId(){ return id; }
    public UUID getOrderId() { return orderId;}
    public String getName(){ return name; }
    public double getTotalPrice() { return totalPrice; }
    public UUID getTransactionId() { return transactionId; }   
    public UUID getSagaId() { return sagaId; }
    public List<ProductQuantityInputDTO> getProductsQuantity() { return productsQuantity; }
}
