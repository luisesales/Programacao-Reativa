package com.ecommerce.order;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import com.ecommerce.order.config.JsonMapConverters;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication(scanBasePackages = "com.ecommerce.order")
@EnableDiscoveryClient
@EnableR2dbcRepositories
public class ECommerceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceOrderApplication.class, args);
	}	

	@Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ObjectMapper objectMapper) {
        return new R2dbcCustomConversions(
            R2dbcCustomConversions.STORE_CONVERSIONS,
            List.of(
                new JsonMapConverters.JsonToMapConverter(objectMapper),
                new JsonMapConverters.MapToJsonConverter(objectMapper)
            )
        );
    }

    @Bean
    public R2dbcMappingContext r2dbcMappingContext(R2dbcCustomConversions conversions) {
        R2dbcMappingContext context = new R2dbcMappingContext();
        context.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        return context;
    }
}


