package com.ecommerce.order.event;

import java.util.UUID;

public record OrderCancelled(UUID sagaId, UUID orderId, String reason) implements OrderEvent {}