package com.ecommerce.order.model.dto;

import java.util.Map;
import java.util.UUID;

public record OrderDTO(UUID id,String name, Map<UUID, Integer> products, Double totalAmount) {}
