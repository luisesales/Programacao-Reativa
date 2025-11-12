package com.ecommerce.transaction.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.repository.TransactionRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
 

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    
    private final TransactionRepository transactionRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public TransactionService(TransactionRepository transactionRepository,                        
                        R2dbcEntityTemplate template
                        ) 
        {
        this.transactionRepository = transactionRepository;
        r2dbcEntityTemplate = template;    
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

    public Mono<Transaction> createTransaction(Order order) {
        logger.info("Creating new transaction reactive: {}", order.getId());
        if (order.getProductsQuantity() == null || order.getProductsQuantity().isEmpty()) {
            logger.warn("Transaction request is empty or invalid.");
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid transaction request: missing products."
            ));
        }
        logger.info("Order processed successfully for products: {}", order.getProductsQuantity());        
        return transactionRepository.save(new Transaction(                
                order.getName(),                
                order.getTotalPrice(),
                order.getId()
            ))
            .flatMap(savedTransaction -> {                                                        
                logger.info("Transaction created with id: {}", savedTransaction.getId());
                return Mono.just(savedTransaction);
            })                                            
            .onErrorResume(e -> {
                logger.error("Error creating Transaction: {}", e.getMessage(), e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Creating Order with message: " + e.getMessage(), e));
            }).doOnSuccess(e -> {
                logger.info("Transaction created successfully with order id: {}", order.getId());
            });  
    }                                                  
} 
    
