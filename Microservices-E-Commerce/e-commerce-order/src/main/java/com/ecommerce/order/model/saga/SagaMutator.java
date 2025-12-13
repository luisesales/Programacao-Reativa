package com.ecommerce.order.model.saga;

import java.Lang.FunctionalInterface;

@FunctionalInterface
public interface SagaMutator {
    void apply(SagaInstance instance, SagaContext context);
}

