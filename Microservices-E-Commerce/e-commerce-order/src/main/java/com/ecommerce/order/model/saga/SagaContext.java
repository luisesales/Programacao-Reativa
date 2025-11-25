package com.ecommerce.order.model.saga;

import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

@Table("saga_context")
public class SagaContext {

    @Id
    private UUID id;
    private double totalPrice;
    private UUID transactionId;
    private UUID stockReservationId;
    private List<ProductQuantityInputDTO> productsQuantity;
    private UUID sagaId;

    public SagaContext(double totalPrice, UUID transactionId, UUID stockReservationId, List<ProductQuantityInputDTO> productsQuantity, UUID sagaId){
        this.totalPrice = totalPrice;
        this.transactionId = transactionId;
        this.stockReservationId = stockReservationId;
        this.productsQuantity = productsQuantity;
        this.sagaId = sagaId;
    }

    public SagaContext(UUID sagaId){
        this.sagaId = sagaId;
        this.totalPrice = 0.0;
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
    public void setProductsQuantity(List<ProductQuantityInputDTO> productsQuantity){
        this.productsQuantity = productsQuantity;
    }
    public void setSagaId(UUID sagaId){
        this.sagaId = sagaId;
    }

    public UUID getId(){ return id; }
    public double getTotalPrice() { return totalPrice; }
    public UUID getTransactionId() { return transactionId; }
    public UUID getStockReservationId() { return stockReservationId; }
    public List<ProductQuantityInputDTO> getProductsQuantity() { return productsQuantity; }
    public UUID getSagaId() { return sagaId; }
}
