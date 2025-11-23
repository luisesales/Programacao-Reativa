package com.ecommerce.order.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cloud.stream.function.StreamBridge;
import reactor.core.publisher.Mono;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaState;
import com.ecommerce.order.model.event.OrderCreatedEvent;

import com.ecommerce.order.repository.SagaRepository;

@Service
public class SagaService {

    private final SagaRepository sagaRepository;
    private final StreamBridge streamBridge;

    @Autowired
    public SagaService(SagaRepository sagaRepository, StreamBridge streamBridge) {
        this.sagaRepository = sagaRepository;
        this.streamBridge = streamBridge;
    }

    public Mono<Void> startSagaForOrder(Order order) {

        SagaInstance saga = new SagaInstance();
        saga.setId(UUID.randomUUID());
        saga.setOrderId(order.getId());
        saga.setState(SagaState.ORDER_CREATED);
        saga.setContext(context);
        saga.setCreatedAt(Instant.now());
        saga.setUpdatedAt(Instant.now());

        SagaContext context = new SagaContext(saga.getId());
        context.setTotalPrice(order.getTotalPrice());
        context.setProductsQuantity(
            order.getProductsQuantity().entrySet().stream()
                .map(e -> new ProductQuantityInputDTO(e.getKey(), e.getValue()))
                .toList()
        );

        );

        OrderCreatedEvent event = new OrderCreatedEvent(
            saga.getId(),
            order.getId(),
            order.getTotalPrice(),
            context.getProductsQuantity()
        );

        return sagaRepository.save(saga)
            .doOnSuccess(s -> {
                streamBridge.send("orderCreated-out-0", event);
            })
            .then();
    }
}
