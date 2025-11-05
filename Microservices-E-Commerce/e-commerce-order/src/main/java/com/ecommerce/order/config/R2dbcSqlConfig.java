package com.ecommerce.order.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;



@Configuration
public class R2dbcSqlConfig{    

    @Bean
    public ApplicationRunner init(DatabaseClient client) {
        return args -> client.sql("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";")
            .fetch().rowsUpdated()
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS orders (
                    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    total_price DECIMAL NOT NULL
                )
            """).fetch().rowsUpdated())
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS order_items (
                    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
                    product_id UUID NOT NULL,
                    quantity INT NOT NULL                    
                )
            """).fetch().rowsUpdated())
            .block();
    }
}
//CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";