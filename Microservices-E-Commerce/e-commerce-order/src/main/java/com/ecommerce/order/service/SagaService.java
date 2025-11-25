package com.ecommerce.order.service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.dto.OrderCreatedEventDTO;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaState;
import com.ecommerce.order.repository.SagaRepository;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;
    private final StreamBridge streamBridge;

    private static final String ORDER_CREATED_BINDING = "orderCreated-out-0";

    @Autowired
    public SagaService(SagaRepository sagaRepository, StreamBridge streamBridge) {
        this.sagaRepository = sagaRepository;
        this.streamBridge = streamBridge;
    }

    public Mono<SagaInstance> startSagaForOrderReactive(Order order) {
        UUID orderId = order.getId();
        
        return sagaRepository.findByOrderId(orderId)
            .switchIfEmpty(createAndPublishSaga(order));
    }

    public Mono<SagaInstance> createAndPublishSaga(Order order) {

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
                OrderCreatedEventDTO event = new OrderCreatedEventDTO(
                    saved.getSagaId(),
                    saved.getOrderId(),
                    context.getTotalPrice(),
                    context.getProductsQuantity()
                );
                
                return Mono.fromCallable(() -> streamBridge.send(ORDER_CREATED_BINDING, event))
                           .subscribeOn(Schedulers.boundedElastic())
                           .flatMap(sent -> {
                               if (Boolean.FALSE.equals(sent)) {                                   
                                   saved.setState(SagaState.TRANSACTION_FAILED); // example error state
                                   return sagaRepository.save(saved).thenReturn(saved);
                               }                               
                               return Mono.just(saved);
                           });
            });
    }

    public Mono<SagaInstance> transitionState(UUID sagaId, SagaState newState, Consumer<SagaInstance> mutator) {
        return sagaRepository.findById(sagaId)
            .flatMap(saga -> {                
                if (saga.getState() == newState) return Mono.just(saga);

                // apply extra context changes
                if (mutator != null) mutator.accept(saga);
                saga.setState(newState);
                saga.setUpdatedAt(Instant.now());
                return sagaRepository.save(saga);
            });
    }
}
