package com.ecommerce.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.order.component.EventPublisher;
import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.StockRejected;
import com.ecommerce.order.event.StockRequested;
import com.ecommerce.order.event.StockReserved;
import com.ecommerce.order.event.OrderCancelled;
import com.ecommerce.order.event.OrderCompleted;
import com.ecommerce.order.event.OrderCreated;
import com.ecommerce.order.event.TransactionApproved;
import com.ecommerce.order.event.TransactionRejected;
import com.ecommerce.order.event.TransactionRequested;
import com.ecommerce.order.model.Product;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;

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

        // === ETAPA 1 — inicia pagamento ===
        case OrderCreated evt -> sagaService.onOrderCreated(evt)
            .then(Mono.fromRunnable(() ->
                eventPublisher.publish(new TransactionRequested(
                    evt.sagaId(), evt.orderId(), evt.name(), evt.totalPrice()
                ))
            ));

        // === ETAPA 2 — inicia reserva de estoque ===
        case TransactionApproved evt -> sagaService
            .onTransactionApproved(evt)          
            .flatMap(products ->
                Mono.fromRunnable(() ->
                    eventPublisher.publish(new StockRequested(
                        evt.sagaId(), evt.orderId(), products
                    ))
                )
            );

        // ETAPA 3 — pedido concluído 
        case StockReserved evt -> sagaService.onStockReserved(evt)
            .then(Mono.fromRunnable(() ->
                eventPublisher.publish(new OrderCompleted(evt.sagaId(), evt.orderId()))
            ));

        // COMPENSAÇÃO — estorno de pagamento
        case TransactionRejected evt -> sagaService.onTransactionRejected(evt)
            .then(Mono.fromRunnable(() ->
                eventPublisher.publish(new OrderCancelled(evt.sagaId(), evt.orderId(), evt.reason()))
            ));

        // COMPENSAÇÃO — devolução de estoque
        case StockRejected evt -> sagaService.onStockRejected(evt)
            .then(Mono.fromRunnable(() ->
                eventPublisher.publish(new OrderCancelled(evt.sagaId(), evt.orderId(), evt.reason()))
            ));

        default -> Mono.error(new IllegalArgumentException("Event not supported by Orchestrator"));
    };
}

}
