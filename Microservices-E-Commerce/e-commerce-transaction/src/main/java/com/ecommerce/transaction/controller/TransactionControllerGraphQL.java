package com.ecommerce.transaction.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.dto.OrderInputDTO;
import com.ecommerce.transaction.service.TransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class TransactionControllerGraphQL {     
    @Autowired   
    private final TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionControllerGraphQL(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @QueryMapping
    public Flux<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @QueryMapping
    public Mono<Transaction> getTransactionById(@Argument UUID id) {
        return transactionService.getTransactionById(id);                                
    }

    
    @QueryMapping
    public Flux<Transaction> getTransactionByOrderId(@Argument UUID id) {
        return transactionService.getTransactionByOrderId(id);                                
    }    

    @MutationMapping
    public Mono<Transaction> createTransaction(@Argument("order") OrderInputDTO orderMono) {
        logger.info("Request received to transaction from: {} with price: {}", orderMono.name(), orderMono.totalPrice());
        return transactionService.createTransaction(orderMono);      
    }
}
