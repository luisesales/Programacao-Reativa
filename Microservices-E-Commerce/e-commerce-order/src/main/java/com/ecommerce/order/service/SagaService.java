package com.ecommerce.order.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.component.EventPublisher;
import com.ecommerce.order.event.DomainEvent;
import com.ecommerce.order.event.OrderCancelled;
import com.ecommerce.order.event.OrderCompleted;
import com.ecommerce.order.event.OrderCreated;
import com.ecommerce.order.event.StockRejected;
import com.ecommerce.order.event.StockReserved;
import com.ecommerce.order.event.TransactionApproved;
import com.ecommerce.order.event.TransactionRejected;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.SagaMutator;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaContextProductsQuantity;
import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaState;
import com.ecommerce.order.repository.SagaContextProductsQuantityRepository;
import com.ecommerce.order.repository.SagaContextRepository;
import com.ecommerce.order.repository.SagaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;
    private final SagaContextRepository sagaContextRepository;
    private final SagaContextProductsQuantityRepository sagaContextProductsQuantityRepository;
    private final EventPublisher eventPublisher;

    public SagaService(SagaRepository sagaRepository, EventPublisher eventPublisher, SagaContextRepository sagaContextRepository,
            SagaContextProductsQuantityRepository sagaContextProductsQuantityRepository) {
        this.sagaRepository = sagaRepository;
        this.eventPublisher = eventPublisher;
        this.sagaContextRepository = sagaContextRepository;
        this.sagaContextProductsQuantityRepository = sagaContextProductsQuantityRepository;
    }

    public Mono<SagaInstance> startSagaForOrderReactive(Order order) {
        UUID orderId = order.getId();

        return sagaRepository.findByOrderId(orderId)
            .switchIfEmpty(createAndPublishSaga(order));
    }

    private Mono<SagaInstance> createAndPublishSaga(Order order) {

        SagaInstance saga = new SagaInstance();        
        saga.setState(SagaState.ORDER_CREATED);

        SagaContext context = new SagaContext(saga.getSagaId());
        context.setOrderId(order.getId());
        context.setTotalPrice(order.getTotalPrice());        
        saga.setContext(context);
        
        Flux.fromIterable(order.getProductsQuantity().entrySet())
        .map(e -> new SagaContextProductsQuantity(
                context.getId(),
                e.getKey(),
                e.getValue()
        ))
        .flatMap(sagaContextProductsQuantityRepository::save)
        .map(saved -> saved.toProductQuantityInputDTO())
        .collectList()           
        .doOnNext(list -> {
            
            context.setProductsQuantity(list);
        });

        return sagaRepository.save(saga)
            .flatMap(saved -> {
                OrderCreated event = new OrderCreated(
                        saved.getSagaId(),
                        context.getOrderId(),
                        saved.getContext().getName(),
                        context.getTotalPrice(),
                        context.getProductsQuantity()
                );                
                eventPublisher.publish(event);
                return Mono.just(saved);
            });
    }


    @Transactional
public Mono<SagaInstance> transitionState(
        DomainEvent event,
        SagaState newState,
        SagaMutator mutator
) {
    return sagaRepository.findById(sagaId)
        .switchIfEmpty(Mono.error(new RuntimeException("Saga not found")))
        .flatMap(instance ->
            sagaContextRepository.findBySagaId(sagaId)                
                .defaultIfEmpty(new SagaContext(sagaId))
                .flatMap(context -> {

                    // Idempotência
                    if (instance.getState() == newState) {
                        return Mono.just(instance);
                    }
                    sagaContextProductsQuantityRepository.findBySagaContextIdProdId(context.getId())
                    if (mutator != null) {
                        mutator.apply(instance, context);
                    }

                    instance.setState(newState);
                    instance.setUpdatedAt(Instant.now());
                    
                    return sagaRepository.save(instance)
                        .flatMap(savedInstance ->
                            sagaContextRepository.save(context)
                                .thenReturn(savedInstance)
                        );
                })
        );
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
            (instance, context) -> {
                context.setTransactionId(event.transactionId());                
            }
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
            (instance, context) -> {
                context.setStockReservationId(event.productId());
            }
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

    public Mono<List<ProductQuantityInputDTO>> getProductsQuantityById(UUID sagaId){
        return sagaContextRepository.findBySagaId(sagaId)
        .flatMap(context ->
            sagaContextProductsQuantityRepository.findBySagaContextId(context.getId())
                .map(SagaContextProductsQuantity::toProductQuantityInputDTO)
                .collectList()
        );
    }

    public Mono<SagaContext> findContextBySagaId(UUID sagaId){
        return sagaContextRepository.findBySagaId(sagaId);
    } 

    @Transactional
    public void saveAll(
        List<SagaInstance> instances,
        List<SagaContext> contexts,
        List<SagaContextProductsQuantity> productsQuantity
    ) {
        sagaRepository.saveAll(instances);
        sagaContextRepository.saveAll(contexts);
    }

    
    // public Mono<SagaInstance> save(SagaInstance saga) {
    //     return sagaRepository.save(saga);
    // }
}
