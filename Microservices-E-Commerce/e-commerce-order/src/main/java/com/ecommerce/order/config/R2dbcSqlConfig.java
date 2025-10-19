package com.ecommerce.order.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class R2dbcSqlConfig {
    @Bean
    public ApplicationRunner init(DatabaseClient client) {
        return args -> client.sql("""
            CREATE TABLE IF NOT EXISTS orders (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                products_quantity CLOB NOT NULL,
                total_price DECIMAL NOT NULL
            );
        """).fetch().rowsUpdated().subscribe();
    }
}
