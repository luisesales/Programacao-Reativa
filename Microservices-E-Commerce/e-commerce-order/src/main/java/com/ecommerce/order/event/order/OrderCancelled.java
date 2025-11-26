package com.ecommerce.order.event.order;

import java.util.UUID;

public record OrderCancelled(UUID sagaId, UUID orderId, String reason) implements OrderEvent {}