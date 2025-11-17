package com.ecommerce.transaction.config;

import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.model.dto.OrderInputDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public class OrderMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrderFromInput(OrderInputDTO input, @MappingTarget Order order;
}