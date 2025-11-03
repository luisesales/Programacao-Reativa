package com.ecommerce.order.config;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.r2dbc.core.DatabaseClient;

import com.fasterxml.jackson.databind.ObjectMapper;



@Configuration
public class R2dbcSqlConfig{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public ApplicationRunner init(DatabaseClient client) {
        return args -> client.sql("""
            CREATE EXTENSION IF NOT EXISTS "uuid-ossp";            
            CREATE TABLE IF NOT EXISTS orders (
                id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                products_quantity JSONB NOT NULL,
                total_price DECIMAL NOT NULL
            );
        """).fetch().rowsUpdated().subscribe();
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
//CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";