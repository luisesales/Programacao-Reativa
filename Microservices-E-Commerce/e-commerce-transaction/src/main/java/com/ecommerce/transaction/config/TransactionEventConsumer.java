package com.ecommerce.transaction.config;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.ecommerce.transaction.event.TransactionRequested;
import com.ecommerce.transaction.service.TransactionService;
import com.ecommerce.transaction.event.TransactionRefundRequested;

@Service
public class TransactionEventConsumer {
     private final TransactionService transactionService;

    public TransactionEventConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Bean
    public Consumer<TransactionRequested> onTransactionRequested() {
        return event -> transactionService.handle(event);
    }

    @Bean
    public Consumer<TransactionRefundRequested> onTransactionRefundRequested() {
        return event -> transactionService.handleRefund(event);
    }
}
