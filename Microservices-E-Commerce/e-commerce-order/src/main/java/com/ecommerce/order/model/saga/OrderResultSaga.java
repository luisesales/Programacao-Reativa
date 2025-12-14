package com.ecommerce.order.model.saga;

import java.util.UUID;

public record OrderResultSaga(
    UUID sagaId,
    UUID orderId,
    SagaState state
) {}
