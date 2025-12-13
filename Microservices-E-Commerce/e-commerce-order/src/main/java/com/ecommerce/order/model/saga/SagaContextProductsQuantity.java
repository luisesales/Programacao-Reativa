package com.ecommerce.order.model.saga;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

@Table("saga_context_products_quantity")
public class SagaContextProductsQuantity {

    @Id
    @Column("id")
    private UUID id;

    @Column("saga_context_id")
    private UUID sagaContextId;

    @Column("product_id")
    private UUID productId;

    @Column("quantity")
    private int quantity;

    @Column("status")
    private ProductStatus status;

    @Column("error")
    private String error;    

    public SagaContextProductsQuantity(UUID sagaContextId, UUID productId, int quantity){
        this.sagaContextId = sagaContextId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = ProductStatus.REQUESTED;
        this.error = "";        
    }

    public SagaContextProductsQuantity(UUID sagaContextId, ProductQuantityInputDTO dto){
        this.sagaContextId = sagaContextId;
        this.productId = dto.productId();
        this.quantity = dto.quantity();
    }
    
    public void setSagaContextId(UUID sagaContextId){
        this.sagaContextId = sagaContextId;
    }
    public void setProductId(UUID productId){
        this.productId = productId;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    public void setProductsQuantity(ProductQuantityInputDTO dto){
        this.productId = dto.productId();
        this.quantity = dto.quantity();
    }
    public void setStatus(ProductStatus status){
        this.status = status;
    }
    public void setError(String error){
        this.error = error;
    }    
    public UUID getId(){ return id; }
    public UUID getSagaContextId(){ return sagaContextId; }
    public UUID getProductId(){ return productId; }
    public ProductStatus getProductStatus(){ return this.status; }
    public String getError(){ return this.error; }
    public ProductQuantityInputDTO toProductQuantityInputDTO(){ return new ProductQuantityInputDTO(this.productId, this.quantity); }
    
}
