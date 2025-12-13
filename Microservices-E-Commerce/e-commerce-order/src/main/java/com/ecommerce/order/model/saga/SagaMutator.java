package com.ecommerce.order.model;

import java.Lang.FunctionalInterface;

import com.ecommerce.order.model.saga.SagaInstance;
import com.ecommerce.order.model.saga.SagaContext;
import com.ecommerce.order.model.saga.SagaContextProductsQuantity;

@FunctionalInterface
public interface SagaMutator {
    void apply(SagaInstance instance, SagaContext context);
}

