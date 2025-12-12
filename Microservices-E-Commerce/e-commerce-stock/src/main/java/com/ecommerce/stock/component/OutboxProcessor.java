package com.ecommerce.stock.component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.event.DomainEvent;
import com.ecommerce.stock.event.StockIncreaseReserved;
import com.ecommerce.stock.event.StockIncreaseRejected;
import com.ecommerce.stock.event.StockRejected;
import com.ecommerce.stock.event.StockReserved;

import com.ecommerce.stock.model.Order;
import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.outbox.OutboxEvent;
import com.ecommerce.stock.model.outbox.OutboxEventContext;
import com.ecommerce.stock.repository.OutboxContextRepository;
import com.ecommerce.stock.repository.OutboxRepository;
import com.ecommerce.stock.repository.ProductRepository;




import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Component
public class OutboxProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);


    private final OutboxRepository outboxRepository;
    private final OutboxContextRepository outboxContextRepository;
    private final EventPublisher eventPublisher;
    private final ProductRepository productRepository;


    public OutboxProcessor(OutboxRepository outboxRepository, EventPublisher eventPublisher, OutboxContextRepository outboxContextRepository, ProductRepository productRepository) {

        this.outboxRepository = outboxRepository;
        this.eventPublisher = eventPublisher;
        this.outboxContextRepository = outboxContextRepository;
        this.productRepository = productRepository;
        start();
    }


    private void start() {
        Flux.defer(outboxRepository::findPendingEvents)
            .flatMap(outboxEvent ->
                outboxContextRepository.findByOutboxEventId(outboxEvent.getId())
                    .collectList()
                    .flatMap(contexts -> processOutboxEvent(outboxEvent, contexts))
            )
            .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(5)))
            .subscribe();
    }

    private Mono<Void> processOutboxEvent(OutboxEvent outboxEvent, List<OutboxEventContext> contexts) {

        UUID productId = getUUID(contexts, "productId");
        UUID orderId   = getUUID(contexts, "orderId");
        UUID sagaId    = getUUID(contexts, "sagaId");
        Integer qty    = getInt(contexts, "quantity");
        Double totalPrice = getDouble(contexts, "totalPrice");
        String error   = getString(contexts, "error");

        return productRepository.findById(productId)
            .map(product ->
                toDomainEvent(outboxEvent.getEventType(), sagaId, orderId, productId, qty,totalPrice, error)
            )
            .flatMap(eventPublisher::publish)         
            .then(outboxRepository.markAsSent(outboxEvent.getId()))            
            .onErrorResume(ex -> handleFailure(outboxEvent, ex))
            .then();  
    }


    private DomainEvent toDomainEvent(String eventType, UUID sagaId, UUID orderId, UUID productId, Integer quantity, Double totalPrice, String error) {        
       return switch (eventType) {

            case "StockReserved" -> new StockReserved(
                sagaId,                
                orderId,
                productId,
                quantity
            );

            case "StockRejected" -> new StockRejected(
                sagaId,                
                orderId,
                productId,
                quantity,
                totalPrice,
                error       
            );

            case "StockIncreaseApproved" -> new StockIncreaseReserved(
                sagaId,                
                orderId,
                productId,
                quantity
            );

            case "StockIncreaseRejected" -> new StockIncreaseRejected(
                sagaId,                
                orderId,
                productId,
                quantity,
                error     
            );
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }

    private Mono<Integer> handleFailure(OutboxEvent event, Throwable ex) {
        if (event.getRetryCount() >= 10) {
            return outboxRepository.markAsFailed(event.getId());
        }
        return outboxRepository.incrementRetry(event.getId(), ex.getMessage());
    }

    private UUID getUUID(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(ctx -> UUID.fromString(ctx.getFieldValue()))
            .orElse(null);
    }
    private Integer getInt(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(ctx -> Integer.valueOf(ctx.getFieldValue()))
            .orElse(null);
    }
    private String getString(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(OutboxEventContext::getFieldValue)
            .orElse(null);
    }
    private Double getDouble(java.util.List<OutboxEventContext> contexts, String fieldName) {
        return contexts.stream()
            .filter(ctx -> fieldName.equals(ctx.getFieldName()))
            .findFirst()
            .map(ctx -> Double.valueOf(ctx.getFieldValue()))
            .orElse(null);
    }

}

