package com.ecommerce.stock.config;

import org.springframework.context.annotation.Configuration;

import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.dto.ProductInputDTO;
import org.mapstruct.*;

@Configuration
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromInput(ProductInputDTO input, @MappingTarget Product product);
}
