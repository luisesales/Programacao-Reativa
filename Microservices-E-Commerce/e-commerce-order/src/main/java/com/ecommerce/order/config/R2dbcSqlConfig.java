package com.ecommerce.order.config;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.core.DatabaseClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.data.r2dbc.dialect.PostgresDialect;



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
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return R2dbcCustomConversions.of(
                PostgresDialect.INSTANCE,
                List.of(
                        new JsonMapConverters.JsonToMapConverter(),
                        new JsonMapConverters.MapToJsonConverter()
                )
        );
    }
}
//CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";