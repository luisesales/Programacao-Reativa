package com.ecommerce.order.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;

import com.ecommerce.order.component.EventPublisher;
import com.ecommerce.order.event.OrderCancelled;
import com.ecommerce.order.event.OrderCompleted;
import com.ecommerce.order.event.OrderCreated;
import com.ecommerce.order.event.StockRejected;
import com.ecommerce.order.event.StockReserved;
import com.ecommerce.order.event.TransactionApproved;
import com.ecommerce.order.event.TransactionRejected;
import com.ecommerce.order.exchange.AiHttpInterface;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.dto.ProductQuantityInputDTO;
import com.ecommerce.order.model.saga.ProductQuantityMutator;
import com.ecommerce.order.model.saga.ProductStatus;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaContextProductsQuantity;
import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaMutator;
import com.ecommerce.order.model.saga.SagaState;
import com.ecommerce.order.repository.SagaContextProductsQuantityRepository;
import com.ecommerce.order.repository.SagaContextRepository;
import com.ecommerce.order.repository.SagaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

@Service
public class SagaService {

    private final AiHttpInterface aiHttpInterface;
    private static final Logger logger = LoggerFactory.getLogger(SagaService.class);

    private final SagaRepository sagaRepository;
    private final SagaContextRepository sagaContextRepository;
    private final SagaContextProductsQuantityRepository sagaContextProductsQuantityRepository;
    private final EventPublisher eventPublisher;

    public SagaService(SagaRepository sagaRepository, EventPublisher eventPublisher, SagaContextRepository sagaContextRepository,
            SagaContextProductsQuantityRepository sagaContextProductsQuantityRepository, AiHttpInterface aiHttpInterface) {
        this.sagaRepository = sagaRepository;
        this.eventPublisher = eventPublisher;
        this.sagaContextRepository = sagaContextRepository;
        this.sagaContextProductsQuantityRepository = sagaContextProductsQuantityRepository;
        this.aiHttpInterface = aiHttpInterface;
    }

    public Mono<SagaInstance> startSagaForOrderReactive(Order order) {
        logger.info("Starting Saga request for order with id {}", order.getId());
        UUID orderId = order.getId();

        return sagaContextRepository.findByOrderId(orderId)
            .flatMap(context-> {
                logger.info("Saga already created for order with id {}", orderId);
                return sagaRepository.findById(context.getSagaId());
            })
            .switchIfEmpty(createAndPublishSaga(order));
            
    }

    public Mono<SagaInstance> createAndPublishSaga(Order order) {

        SagaInstance saga = SagaInstance.create(SagaState.ORDER_CREATED);

        return sagaRepository.save(saga)
            .flatMap(savedSaga ->
                sagaContextRepository.save(
                    SagaContext.from(order, savedSaga.getSagaId())
                )
                .flatMap(context ->
                    Flux.fromIterable(order.getProductsQuantity().entrySet())
                        .flatMap(e ->
                            sagaContextProductsQuantityRepository.save(
                                SagaContextProductsQuantity.from(context, e)
                            )
                        )
                        .thenReturn(savedSaga)
                )
            )
            .doOnSuccess(savedSaga ->
                eventPublisher.publish(
                    new OrderCreated(
                        savedSaga.getSagaId(),
                        order.getId(),
                        order.getName(),
                        order.getTotalPrice(),
                        order.getProductsQuantity()
                    )
                )
            );
    }





    public Mono<SagaInstance> transitionState(
        UUID sagaId,
        SagaState newState,
        SagaMutator mutator,
        ProductQuantityMutator productQuantityMutator,
        UUID productId
    ) {
        logger.info("Transitioning saga {} to {}", sagaId, newState);

        return advanceSagaState(sagaId, newState)
            .flatMap(savedSaga ->

                sagaContextRepository.findBySagaId(sagaId)
                    .flatMap(context -> {

                        if (mutator != null) {
                            mutator.apply(savedSaga, context);
                        }

                        Mono<Void> contextSave =
                            sagaContextRepository.save(context).then();

                        Mono<Void> productUpdate =
                            productQuantityMutator != null
                                ? updateProductsQuantity(context.getId(), productQuantityMutator, productId)
                                : Mono.empty();

                        return Mono.when(contextSave, productUpdate)
                            .thenReturn(savedSaga);
                    })
            )
            .doOnSuccess(s -> logger.info("Saga {} transitioned to {}", sagaId, newState));
    }



    public Mono<SagaInstance> onOrderCreated(OrderCreated event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_CREATED,
            null,
            (instance) -> instance.setStatus(ProductStatus.REQUESTED),
            null
        );
    }
    public Mono<SagaInstance> onTransactionApproved(TransactionApproved event) {
        return transitionState(
            event.sagaId(),
            SagaState.TRANSACTION_CONFIRMED,
            (instance, context) -> {
                context.setTransactionId(event.transactionId());                
            },
            null,
            null
        );
    }
    public Mono<SagaInstance> onTransactionRejected(TransactionRejected event) {
        return transitionState(
            event.sagaId(),
            SagaState.TRANSACTION_FAILED,
            null,
            (instance) -> {
                instance.setStatus(ProductStatus.ERROR);
            },
            null
        );
    }
    public Mono<SagaInstance> onStockReserved(StockReserved event) {
        return transitionState(
            event.sagaId(),
            SagaState.STOCK_RESERVED,
            null,
            (instance) -> {
                instance.setStatus(ProductStatus.APPROVED);
            },
            event.productId()
        );
    }
    public Mono<SagaInstance> onStockRejected(StockRejected event) {
        return transitionState(
            event.sagaId(),
            SagaState.STOCK_FAILED,
            null,
            (instance) -> {
                instance.setError(event.reason());
                instance.setStatus(ProductStatus.REJECTED);
            },
            event.productId()
        );
    }
    public Mono<SagaInstance> onOrderCompleted(OrderCompleted event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_COMPLETED,
            null,
            null,
            null          
        );
    }
    public Mono<SagaInstance> onOrderCancelled(OrderCancelled event) {
        return transitionState(
            event.sagaId(),
            SagaState.ORDER_COMPENSATED,
            null,
            (instance) -> {
                instance.setStatus(ProductStatus.ERROR);
            },
            null
        );
    }
    public Mono<SagaInstance> findById(UUID sagaId) {        
        return sagaContextRepository.findBySagaId(sagaId)
        .flatMap(context ->
            sagaRepository.findById(context.getSagaId())
                .map(saga -> {
                    saga.setContext(context);
                    return saga;
                })
        );
    }
    public Mono<SagaInstance> findByOrderId(UUID orderId) {
        logger.info("Fetching Saga by orderId {}", orderId);
        return sagaContextRepository.findByOrderId(orderId)
        .flatMap(context ->
            sagaRepository.findById(context.getSagaId())
                .map(saga -> {
                    logger.info("Saga found with orderId {}",orderId);
                    saga.setContext(context);
                    return saga;
                })
        )
        .switchIfEmpty(
            Mono.defer(() -> {
                logger.error("Saga with orderId {} not found",orderId);
                return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Saga with orderId "+ orderId + " not found"));
            })
        );
    }

    public Mono<List<ProductQuantityInputDTO>> getProductsQuantityById(UUID sagaId){
        logger.info("Fetching ProductsQuantity for Saga with id {}",sagaId);
        return sagaContextRepository.findBySagaId(sagaId)
        .flatMap(context ->
            sagaContextProductsQuantityRepository.findBySagaContextId(context.getId())
                .map(SagaContextProductsQuantity::toProductQuantityInputDTO)
                .collectList()
        ).doOnSuccess(saga -> logger.info("SagaContextProductsQuantity found with sagaId {}",sagaId))
        .onErrorResume(e -> {
                logger.error("Error fetching SagaContextProductsQuantity with sagaId {} with error: {}",sagaId,e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching SagaContextProductsQuantityContext with sagaId " + sagaId + " with error: " + e));
            });
    }

    public Mono<SagaContext> findContextBySagaId(UUID sagaId){
        logger.info("Fetching SagaContext for Saga with id {}",sagaId);
        return sagaContextRepository.findBySagaId(sagaId)
            .switchIfEmpty(
                Mono.defer(() -> {
                    logger.error("SagaContext with sagaId {} not found",sagaId);
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "SagaContext with sagaId "+ sagaId + " not found"));
                })
            )
            .onErrorResume(e -> {
                logger.error("Error fetching SagaContext with sagaId {} with error: {}",sagaId,e);
                return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching SagaContext with sagaId " + sagaId + " with error: " + e));
            });
    } 

    @Transactional
    public void saveAll(
        List<SagaInstance> instances,
        List<SagaContext> contexts        
    ) {
        sagaRepository.saveAll(instances);
        sagaContextRepository.saveAll(contexts);
    }

    private Mono<Void> updateProductsQuantity(UUID sagaContextId, ProductQuantityMutator mutator, UUID productId){        
        // if(productId == null){
        //     return sagaContextProductsQuantityRepository.findBySagaContextId(sagaContextId)
        //         .flatMapMany(pq -> {
        //             logger.info("Updating SagaContextProductQuantity with productId {} for SagaContext with {}", pq.getProductId(), sagaContextId);
        //             mutator.apply(pq)
        //                 .then(sagaContextProductsQuantityRepository.save(pq));
        //         }).then();
        // }
        // else{
        //     return sagaContextProductsQuantityRepository.findBySagaContextIdProdId(sagaContextId, productId)
        //         .flatMap(pq -> {
        //             logger.info("Updating SagaContextProductQuantity with productId {} for SagaContext with {}", pq.getProductId(), sagaContextId);
        //             mutator.apply(pq)
        //                 .then(sagaContextProductsQuantityRepository.save(pq));
        //         }).then();
        // }
        return (productId == null
            ? sagaContextProductsQuantityRepository
                .findBySagaContextId(sagaContextId)
            : sagaContextProductsQuantityRepository
                .findBySagaContextIdProdId(sagaContextId, productId)
        )
        .flatMap(pq -> {
            logger.info("Updating SagaContextProductsQuantity with productId {} for SagaContext with {}", pq.getProductId(), sagaContextId);
            mutator.apply(pq)
                .then(sagaContextProductsQuantityRepository.save(pq))
                .onErrorResume(e -> {
                    logger.error("Error updating SagaContextProductsQuantity with sagaContextId {} and productId {} with error: {}",sagaContextId,pq.getProductId(),e);
                    return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating SagaContextProductsQuantity with sagaContextId " + sagaContextId + " and productId " + pq.getProductId() + " with error: " + e));
                });
        })
        .then();
    }

    public Flux<SagaContextProductsQuantity> findProductsQuantityByOrderId(UUID orderId){
        return sagaContextRepository.findByOrderId(orderId)
            .flatMapMany(context -> 
                sagaContextProductsQuantityRepository.findBySagaContextId(context.getId())
                .switchIfEmpty(
                Flux.defer(() -> {
                    logger.error("SagaContextProductsQuantity with order id {} not found", orderId);
                    return Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or access denied"));
                })
            ))
            .switchIfEmpty(
                Flux.defer(() -> {
                    logger.error("SagaContext with order id {} not found", orderId);
                    return Flux.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found or access denied"));
                })
            )
            .onErrorResume(e -> {
                    logger.error("Error fetching SagaContext with order id {}", orderId);
                    return Flux.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error fetching SagaContext with order id " + orderId));
            });
    }

    public Mono<SagaInstance> advanceSagaState(UUID sagaId, SagaState newState) {
        return sagaRepository.findById(sagaId)
            .switchIfEmpty(Mono.error(new IllegalStateException("Saga not found")))
            .flatMap(saga -> {

                if (saga.getState() == newState) {
                    return Mono.just(saga);
                }

                saga.setState(newState);
                saga.setUpdatedAt(Instant.now());

                return sagaRepository.save(saga);
            })
            .retryWhen(
                reactor.util.retry.Retry.max(3)
                    .filter(e -> e instanceof OptimisticLockingFailureException)
            );
    }



}
