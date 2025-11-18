package com.ecommerce.transaction.config;

import com.ecommerce.transaction.model.Order;
import com.ecommerce.transaction.model.dto.OrderInputDTO;
import com.ecommerce.transaction.model.dto.ProductQuantityInputDTO;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    default HashMap<UUID, Integer> mapProducts(List<ProductQuantityInputDTO> list) {

        if (list == null) return new HashMap<>();

        return list.stream()
                .collect(Collectors.toMap(
                        ProductQuantityInputDTO::productId,
                        ProductQuantityInputDTO::quantity,
                        (a, b) -> a,               // Resolve duplicates
                        HashMap::new
                ));
    }
    OrderInputDTO toDTO(Order order);

}