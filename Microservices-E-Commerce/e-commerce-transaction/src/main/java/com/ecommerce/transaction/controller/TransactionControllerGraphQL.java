package com.ecommerce.transaction.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;



import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.model.Transaction;
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
    public Mono<Transaction> createTransaction(@Argument Mono<Order> orderMono) {
        return orderMono
        .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
        .flatMap(transaction -> {
            logger.info("Request received to transaction from: {} with price: {}", transaction.getName(), transaction.getTotalPrice());
            return transactionService.createTransaction(transaction);
        });        
    }
}
