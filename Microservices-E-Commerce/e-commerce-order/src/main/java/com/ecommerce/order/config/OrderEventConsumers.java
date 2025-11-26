package com.ecommerce.order.config;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.order.OrderCreated;
import com.ecommerce.order.event.stock.StockRejected;
import com.ecommerce.order.event.stock.StockReserved;
import com.ecommerce.order.event.transaction.TransactionApproved;
import com.ecommerce.order.event.transaction.TransactionRejected;
import com.ecommerce.order.service.OrderOrchestrator;

@Configuration
public class OrderEventConsumers {
    
    private final OrderOrchestrator orchestrator;

    public OrderEventConsumers(OrderOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Bean
    public Consumer<OrderCreated> orderCreated() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<TransactionApproved> transactionConfirmed() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<TransactionRejected> transactionFailed() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<StockReserved> stockReserved() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<StockRejected> stockFailed() {
        return event -> orchestrator.handle(event).subscribe();
    }
}

