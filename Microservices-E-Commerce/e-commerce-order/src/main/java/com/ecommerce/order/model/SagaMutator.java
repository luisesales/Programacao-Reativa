package com.ecommerce.order.model;

import java.Lang.FunctionalInterface;

import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaContext;

@FunctionalInterface
public interface SagaMutator {
    void apply(SagaInstance instance, SagaContext context);
}

