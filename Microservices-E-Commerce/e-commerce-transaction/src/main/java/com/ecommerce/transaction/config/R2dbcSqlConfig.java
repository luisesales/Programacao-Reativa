package com.ecommerce.transaction.config;

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
                CREATE TABLE IF NOT EXISTS transactions (
                    id UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    total_price DECIMAL NOT NULL,
                    order_id UUID NOT NULL
                )
            """).fetch().rowsUpdated())
            .block();
    }
}
//CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";