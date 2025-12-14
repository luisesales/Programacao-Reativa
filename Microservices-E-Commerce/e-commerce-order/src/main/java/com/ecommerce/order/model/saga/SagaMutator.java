package com.ecommerce.order.model.saga;

@FunctionalInterface
public interface SagaMutator {
    void apply(SagaInstance instance, SagaContext context);
}

