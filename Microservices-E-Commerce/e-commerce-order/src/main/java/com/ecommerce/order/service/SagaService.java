package com.ecommerce.order.service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.ecommerce.order.component.EventPublisher;
import com.ecommerce.order.event.StockRejected;
import com.ecommerce.order.event.StockReserved;
import com.ecommerce.order.event.OrderCancelled;
import com.ecommerce.order.event.OrderCompleted;
import com.ecommerce.order.event.OrderCreated;
import com.ecommerce.order.event.TransactionApproved;
import com.ecommerce.order.event.TransactionRejected;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaState;
import com.ecommerce.order.model.saga.SagaContextProductsQuantity;
import com.ecommerce.order.repository.SagaRepository;
import com.ecommerce.order.repository.SagaContextRepository;


import reactor.core.publisher.Mono;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;
    private final SagaContextRepository sagaContextRepository;
    private final SagaContextProductsQuantity sagaContextProductsQuantity;
    private final EventPublisher eventPublisher;

    public SagaService(SagaRepository sagaRepository, EventPublisher eventPublisher, SagaContextRepository sagaContextRepository,
            SagaContextProductsQuantity sagaContextProductsQuantity) {
        this.sagaRepository = sagaRepository;
        this.eventPublisher = eventPublisher;
        this.sagaContextRepository = sagaContextRepository;
        this.sagaContextProductsQuantity = sagaContextProductsQuantity;
    }

    public Mono<SagaInstance> startSagaForOrderReactive(Order order) {
        UUID orderId = order.getId();

        return sagaRepository.findByOrderId(orderId)
            .switchIfEmpty(createAndPublishSaga(order));
    }

    private Mono<SagaInstance> createAndPublishSaga(Order order) {

        SagaInstance saga = new SagaInstance();
        saga.setOrderId(order.getId());
        saga.setState(SagaState.ORDER_CREATED);

        SagaContext context = new SagaContext(saga.getSagaId());
        context.setTotalPrice(order.getTotalPrice());
        context.setProductsQuantity(
            order.getProductsQuantity().entrySet().stream()
                .map(e -> new ProductQuantityInputDTO(e.getKey(), e.getValue()))
                .toList()
        );
        saga.setContext(context);

        return sagaRepository.save(saga)
            .flatMap(saved -> {
                OrderCreated event = new OrderCreated(
                        saved.getSagaId(),
                        saved.getOrderId(),
                        saved.getContext().getName(),
                        context.getTotalPrice(),
                        context.getProductsQuantity()
                );

                
                eventPublisher.publish(event);

                return Mono.just(saved);
            });
    }


    public Mono<SagaInstance> transitionState(
            UUID sagaId,
            SagaState newState,
            Consumer<SagaInstance> mutator
    ) {
        return sagaRepository.findById(sagaId)
            .flatMap(saga -> {

                // idempotência
                if (saga.getState() == newState) return Mono.just(saga);

                // atualiza context se necessário
                if (mutator != null) mutator.accept(saga);

                saga.setState(newState);
                saga.setUpdatedAt(Instant.now());

                return sagaRepository.save(saga);
            });
    }

    public Mono<SagaInstance> onOrderCreated(OrderCreated event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_CREATED,
            null
        );
    }
    public Mono<SagaInstance> onTransactionApproved(TransactionApproved event) {
        return transitionState(
            event.sagaId(),
            SagaState.TRANSACTION_CONFIRMED,
            null
        );
    }
    public Mono<SagaInstance> onTransactionRejected(TransactionRejected event) {
        return transitionState(
            event.sagaId(),
            SagaState.TRANSACTION_FAILED,
            null
        );
    }
    public Mono<SagaInstance> onStockReserved(StockReserved event) {
        return transitionState(
            event.sagaId(),
            SagaState.STOCK_RESERVED,
            null
        );
    }
    public Mono<SagaInstance> onStockRejected(StockRejected event) {
        return transitionState(
            event.sagaId(),
            SagaState.STOCK_FAILED,
            null
        );
    }
    public Mono<SagaInstance> onOrderCompleted(OrderCompleted event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_COMPLETED,
            null
        );
    }
    public Mono<SagaInstance> onOrderCancelled(OrderCancelled event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_COMPENSATED,
            null
        );
    }
    public Mono<SagaInstance> findById(UUID sagaId) {
        return sagaRepository.findById(sagaId);
    }
    public Mono<SagaInstance> findByOrderId(UUID orderId) {
        return sagaRepository.findByOrderId(orderId);
    }
    // public Mono<SagaInstance> save(SagaInstance saga) {
    //     return sagaRepository.save(saga);
    // }
}
