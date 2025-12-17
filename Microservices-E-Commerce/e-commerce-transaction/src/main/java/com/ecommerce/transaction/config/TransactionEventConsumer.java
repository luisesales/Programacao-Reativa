package com.ecommerce.transaction.config;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.ecommerce.transaction.event.TransactionRefundRequested;
import com.ecommerce.transaction.event.TransactionRequested;
import com.ecommerce.transaction.service.TransactionService;

@Service
public class TransactionEventConsumer {
    private static final Logger logger =
        LoggerFactory.getLogger(TransactionEventConsumer.class);

     private final TransactionService transactionService;

    public TransactionEventConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Bean
    public Consumer<TransactionRequested> transactionRequested() {
        logger.info("Consuming TransactionRequested event");
        return event -> transactionService.handle(event);
    }

    @Bean
    public Consumer<TransactionRefundRequested> transactionRefundRequested() {
        logger.info("Consuming TransactionRefundRequested event");
        return event -> transactionService.handleRefund(event);
    }
}
