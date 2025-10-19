package com.ecommerce.stock.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class R2dbcSqlConfig {
    @Bean
    public ApplicationRunner init(DatabaseClient client) {
         return args -> client.sql("""
            CREATE TABLE IF NOT EXISTS product (
                id VARCHAR(50) PRIMARY KEY,
                name VARCHAR(255),
                description VARCHAR(500),
                price DECIMAL,
                stock_quantity INT,
                category VARCHAR(255)
            );
        """).fetch().rowsUpdated().subscribe();
    }
}