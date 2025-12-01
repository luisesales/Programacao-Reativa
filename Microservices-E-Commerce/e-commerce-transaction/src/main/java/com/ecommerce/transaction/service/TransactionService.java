package com.ecommerce.transaction.service;

import java.time.LocalDateTime;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.transaction.component.EventPublisher;
import com.ecommerce.transaction.event.TransactionRequested;
import com.ecommerce.transaction.event.TransactionRefundRequested;
import com.ecommerce.transaction.event.transaction.refund.*;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.dto.OrderInputDTO;
import com.ecommerce.transaction.model.outbox.OutboxEvent;
import com.ecommerce.transaction.model.outbox.OutboxEventContext;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.repository.OutboxRepository;
import com.ecommerce.transaction.repository.OutboxContextRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
 

@Service
public class TransactionService {
    private AtomicInteger currentBalance = new AtomicInteger(100000);
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    
    private final TransactionRepository transactionRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EventPublisher eventPublisher;
    private final OutboxRepository outboxRepository;
    private final OutboxContextRepository outboxContextRepository;

    public TransactionService(TransactionRepository transactionRepository,                        
                        R2dbcEntityTemplate template,
                        EventPublisher eventPublisher, 
                        OutboxRepository outboxRepository,
                        OutboxContextRepository outboxContextRepository
                        ) 
        {
        this.transactionRepository = transactionRepository;
        r2dbcEntityTemplate = template; 
        this.eventPublisher = eventPublisher;   
        this.outboxRepository = outboxRepository;
        this.outboxContextRepository = outboxContextRepository;        
    }

    public Flux<Transaction> getAllTransactions() {        
        logger.info("Fetching all transactions (reactive)");
        return transactionRepository.findAll()
            .doOnError(e -> {
                logger.error("Error fetching all transactions", e);
                Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Retrieving all Transactions with message: " + e.getMessage(), e));
            });

    }
    public Mono<Transaction> getTransactionById(UUID id) {
        logger.info("Fetching transaction with id: {}", id);
        return transactionRepository.findById(id)                  
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found or access denied")))
                              .doOnError(e -> {
                                logger.error("Error fetching transaction id " + id, e);
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Retrieving Transaction with id "+ id + " message: " + e.getMessage(), e));
            });
    }

    public Mono<Transaction> getTransactionByOrderId(UUID orderId) {
        logger.info("Fetching transactions with order id: {}", orderId);
         return transactionRepository.findByOrderId(orderId)                  
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transactions not found or access denied")))
                              .doOnError(e -> {
                                logger.error("Error fetching transaction id " + orderId, e);
                                Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Retrieving Transaction with Order id "+ orderId + " message: " + e.getMessage(), e));
            });
    }

    public Mono<Transaction> createTransaction(OrderInputDTO order) {
        if (order == null) {
            logger.warn("Transaction request is empty or invalid.");
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Request body is missing"
            ));
        }
        if(
            order.name() == null || 
            order.id() == null || 
            order.productsQuantity() == null ||
            order.totalPrice() == null
        )
        {
            logger.warn("Transaction request is empty or invalid.");
            return Mono.error(new ResponseStatusException(                
                    HttpStatus.BAD_REQUEST, "Invalid request body: missing required fields"
            ));
        }
        logger.info("Creating new transaction reactive: {}", order.id());    
        logger.info("Order processed successfully for products: {}", order.productsQuantity());        
        return transactionRepository.save(new Transaction(                
                order.name(),                
                order.totalPrice(),
                order.id()
            ))
            .flatMap(savedTransaction -> {                                                        
                logger.info("Transaction created with id: {}", savedTransaction.getId());
                return Mono.just(savedTransaction);
            })                                            
            .onErrorResume(e -> {
                logger.error("Error creating Transaction: {}", e.getMessage(), e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Order with message: " + e.getMessage(), e));
            }).doOnSuccess(e -> {
                logger.info("Transaction created successfully with order id: {}", order.id());
            });  
    } 
    
    public Mono<Void> handle(TransactionRequested event) {
        return transactionRepository.findByOrderId(event.orderId())
        .flatMap(existing -> {
            logger.info("Event already processed for saga {}", event.sagaId());
            return Mono.empty();
        })
        .switchIfEmpty(processNewTransaction(event));
    }
    public Mono<Void> processNewTransaction(TransactionRequested event) {
        logger.info("Processing transaction for order {} (saga: {})",
                event.orderId(), event.sagaId());

        return transactionRepository.save(
                new Transaction(
                        event.name(),                        
                        event.totalPrice(),
                        event.orderId()
                )
        ).flatMap(saved -> {
            logger.info("Transaction saved (id {}) for order {}", saved.getId(), saved.getOrderId());

            boolean approved = isPaymentApproved(event);

            String eventType = approved ? "TransactionApproved" : "TransactionRejected";

            OutboxEvent outbox = new OutboxEvent(                
                "TRANSACTION",
                eventType,
                false,
                LocalDateTime.now()
            );

            return outboxRepository.save(outbox)
                .thenMany(Flux.just(
                    new OutboxEventContext(outbox.getId(), "sagaId", String.valueOf(event.sagaId()),saved.getId()),
                    new OutboxEventContext(outbox.getId(), "orderId", String.valueOf(event.orderId()),saved.getId()),
                    new OutboxEventContext(outbox.getId(), "transactionId", String.valueOf(saved.getId()),saved.getId())
                ).flatMap(outboxContextRepository::save))
                .then();
            
        });
    }

    public Mono<Void> handleRefund(TransactionRefundRequested event) {
        logger.info("Processing refund for order {} (saga: {})",
                event.orderId(), event.sagaId());

        currentBalance.getAndAdd((int) Math.round(event.totalPrice()));
        eventPublisher.publish(
            new TransactionRefundApproved(event.sagaId(), event.orderId(), UUID.randomUUID())
        );
        logger.info("Refund processed for order {}: amount {}", event.orderId(), event.totalPrice());

        return Mono.empty();
    }
    

    private boolean isPaymentApproved(TransactionRequested event) {        
        return currentBalance.getAndAdd((int) Math.round(-event.totalPrice())) >= 0;
    }
} 
    
