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

import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.dto.OrderInputDTO;
import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.service.TransactionService;

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
    public Mono<Transaction> getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id);
                                
    }
    
    @GetMapping(path = "/order/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Transaction> getTransactionByOrderId(@PathVariable UUID id) {
        return transactionService.getTransactionByOrderId(id);
                                
    }

    @PostMapping
    public Mono<Transaction> createTransaction(@RequestBody Mono<OrderInputDTO> orderMono) {
        return orderMono
        .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Order body is missing"
            )))
        .flatMap(orderInput -> {
        //     OrderInputDTO dto = new OrderInputDTO(
        //     orderInput.getId(),
        //     orderInput.getName(),
        //     orderInput.getProductsQuantity(),
        //     orderInput.getTotalPrice()
        // );
            logger.info("Request received to transaction from: {} with price: {}", orderInput.name(), orderInput.totalPrice());
            return transactionService.createTransaction(dto);
        });        
    }
}
