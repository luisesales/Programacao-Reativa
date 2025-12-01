package com.ecommerce.transaction.model.outbox;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("outbox_event")
public class OutboxEvent {
    @Id
    @Column("id")
    private UUID id;
    @Column("aggregate_type")
    private String aggregateType;
    @Column("event_type")
    private String eventType;
    @Column("published")
    private Boolean published;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("published_at")
    private LocalDateTime publishedAt;

    private Integer retryCount;
    private String lastError;
    private LocalDateTime lastAttemptAt;

    public OutboxEvent(String aggregateType, String eventType, Boolean published, LocalDateTime createdAt) {        
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.published = published;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getAggregateType() {
        return aggregateType;
    }
    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }
    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    public Boolean getPublished() {
        return published;
    }
    public void setPublished(Boolean published) {
        this.published = published;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    public Integer getRetryCount() {
        return retryCount;
    }
    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
    public String getLastError() {
        return lastError;
    }
    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }
    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }
}
