package com.ecommerce.order.model.saga;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("saga_instance")
public class SagaInstance {
    @Id
    @Column("id")
    private UUID sagaId;

    @Column("state")
    private SagaState state;

    @Transient
    private SagaContext context;

    @Column("created_at")
    private Instant createdAt;
    @Column("updated_at")
    private Instant updatedAt;

    @Version
    private Long version; 

    public static SagaInstance create(SagaState initialState) {
        SagaInstance saga = new SagaInstance();
        saga.setState(initialState);
        return saga;
    }


    public SagaInstance() {            
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();        
    }

    public UUID getSagaId() { 
        return sagaId; 
    }
    public void setSagaId(UUID sagaId) { 
        this.sagaId = sagaId; 
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
}
