package com.ecommerce.order.model.saga;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Table("saga_instance")
public class SagaInstance {
    @Id
    @Column("id")
    private UUID sagaId;

    @NotBlank(message="Order ID is necessary")
    @Column("order_id")
    private UUID orderId;

    @Column("state")
    private SagaState state;

    @NotEmpty(message="context is necessary")
    @Column("context")
    private SagaContext context;

    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    private Long version; 

    public SagaInstance() {
        this.sagaId = UUID.randomUUID();
        this.context = new SagaContext(sagaId);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.version = 0L;
    }

    public UUID getSagaId() { 
        return sagaId; 
    }
    public void setSagaId(UUID sagaId) { 
        this.sagaId = sagaId; 
    }

    public UUID getOrderId() { 
        return orderId; 
    }
    public void setOrderId(UUID orderId) { 
        this.orderId = orderId; 
    }

    public void setState(SagaState state) {
        this.state = state;
        this.updatedAt = Instant.now();
    }

    public void setContext(SagaContext context) { 
        this.context = context; 
    }

    public void setUpdatedAt(Instant updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public SagaContext getContext() { 
        return context; 
    }

    public SagaState getState() { 
        return state; 
    }

    public Instant getCreatedAt() { 
        return createdAt; 
    }

    public Instant getUpdatedAt() { 
        return updatedAt; 
    }

    public Long getVersion() { 
        return version; 
    }

    public void incrementVersion() { 
        this.version++; 
    }
}
