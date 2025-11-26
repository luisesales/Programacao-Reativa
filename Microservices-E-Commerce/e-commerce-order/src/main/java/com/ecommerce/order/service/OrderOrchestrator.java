package com.ecommerce.order.service;

import org.springframework.stereotype.Service;

import com.ecommerce.order.component.EventPublisher;
import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.order.OrderCancelled;
import com.ecommerce.order.event.order.OrderCompleted;
import com.ecommerce.order.event.order.OrderCreated;
import com.ecommerce.order.event.stock.StockRejected;
import com.ecommerce.order.event.stock.StockRequested;
import com.ecommerce.order.event.stock.StockReserved;
import com.ecommerce.order.event.transaction.TransactionApproved;
import com.ecommerce.order.event.transaction.TransactionRejected;
import com.ecommerce.order.event.transaction.TransactionRequested;

import reactor.core.publisher.Mono;

@Service
public class OrderOrchestrator {

    private final SagaService sagaService;
    private final EventPublisher eventPublisher;

    public OrderOrchestrator(SagaService sagaService, EventPublisher eventPublisher) {
        this.sagaService = sagaService;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Void> handle(DomainEvent event) {
        return switch (event) {

            // ETAPA 1 — inicia pagamento
            case OrderCreated evt -> sagaService.onOrderCreated(evt)
                .then(Mono.fromRunnable(() ->
                    eventPublisher.publish(new TransactionRequested(evt.sagaId(), evt.orderId(), evt.name(), evt.totalPrice()))
                ));

            // ETAPA 2 — inicia reserva de estoque
            case TransactionApproved evt -> sagaService.onTransactionApproved(evt)
                .then(Mono.fromRunnable(() ->
                    eventPublisher.publish(new StockRequested(evt.sagaId(), evt.orderId()))
                ));

            // ETAPA 3 — conclui pedido
            case StockReserved evt -> sagaService.onStockReserved(evt)
                .then(Mono.fromRunnable(() ->
                    eventPublisher.publish(new OrderCompleted(evt.sagaId(), evt.orderId()))
                ));


            // COMPENSAÇÃO — reverte pagamento
            case TransactionRejected evt -> sagaService.onTransactionRejected(evt)
                .then(Mono.fromRunnable(() ->
                    eventPublisher.publish(new OrderCancelled(evt.sagaId(), evt.orderId(), evt.reason()))
                ));

            // COMPENSAÇÃO — reverte reserva de estoque
            case StockRejected evt -> sagaService.onStockRejected(evt)
                .then(Mono.fromRunnable(() ->
                    eventPublisher.publish(new OrderCancelled(evt.sagaId(), evt.orderId(), evt.reason()))
                ));

            default -> Mono.error(new IllegalArgumentException("Event not supported by Orchestrator"));
        };
    }
}
