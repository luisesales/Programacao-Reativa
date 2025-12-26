package com.ecommerce.stock.config;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.ecommerce.stock.model.Product;
import com.ecommerce.stock.model.dto.ProductInputDTO;

@Configuration
@Mapper(componentModel = "spring")
@Component
public interface ProductMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProductFromInput(ProductInputDTO input, @MappingTarget Product product);
}