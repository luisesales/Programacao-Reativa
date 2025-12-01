package com.ecommerce.order.event;

import java.util.UUID;

public record OrderCompleted(UUID sagaId, UUID orderId) implements OrderEvent {}
