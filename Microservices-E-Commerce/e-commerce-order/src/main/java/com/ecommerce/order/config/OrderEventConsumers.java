package com.ecommerce.order.config;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecommerce.order.event.OrderCreated;
import com.ecommerce.order.event.StockIncreaseRejected;
import com.ecommerce.order.event.StockIncreaseReserved;
import com.ecommerce.order.event.StockRejected;
import com.ecommerce.order.event.StockReserved;
import com.ecommerce.order.event.TransactionApproved;
import com.ecommerce.order.event.TransactionRefundApproved;
import com.ecommerce.order.event.TransactionRefundRejected;
import com.ecommerce.order.event.TransactionRejected;
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
    public Consumer<TransactionRefundApproved> transactionRefundApproved() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<TransactionRefundRejected> transactionRefundRejected() {
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

    @Bean
    public Consumer<StockIncreaseReserved> stockIncreaseReserved() {
        return event -> orchestrator.handle(event).subscribe();
    }

    @Bean
    public Consumer<StockIncreaseRejected> stockIncreaseRejected() {
        return event -> orchestrator.handle(event).subscribe();
    }
}

