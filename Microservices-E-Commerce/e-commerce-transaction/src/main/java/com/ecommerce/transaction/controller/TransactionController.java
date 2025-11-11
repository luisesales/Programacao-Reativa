package com.ecommerce.transaction.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.model.OrderResult;
import com.ecommerce.transaction.model.Product;
import com.ecommerce.transaction.service.TransactionService;
import com.ecommerce.transaction.model.dto.OrderDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/transactions")
public class TransactionController {
    @Autowired
    private final TransactionService transactionService;

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping(path = "/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<TransactionDTO> getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id);
                                
    }

    @PostMapping
    public Mono<Transaction> createTransaction(@RequestBody Mono<Transaction> transactionMono) {
        return transactionMono
        .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transaction body is missing"
            )))
        .flatMap(transaction -> {
            logger.info("Request received to transaction from: {} with price: {}", transaction.getName(), transaction.getTotalPrice());
            return transactionService.createTransaction(transaction);
        });        
    }
}
