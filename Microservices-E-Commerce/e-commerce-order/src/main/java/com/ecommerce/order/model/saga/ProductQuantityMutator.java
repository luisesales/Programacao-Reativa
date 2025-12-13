package com.ecommerce.order.model.saga;

@FunctionalInterface
public interface  ProductQuantityMutator {
    void apply(SagaContextProductsQuantity instance);
}
