package com.ecommerce.order.config;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;
import io.r2dbc.spi.ConnectionFactory;



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
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        var dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(
            dialect,
            List.of(
                new MapToJsonConverter(objectMapper),
                new JsonToMapConverter(objectMapper)
            )
        );
    }

    @WritingConverter
    public static class MapToJsonConverter implements Converter<Map<UUID, Integer>, Json> {
        private final ObjectMapper objectMapper;

        public MapToJsonConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Json convert(Map<UUID, Integer> source) {
            try {
                return Json.of(objectMapper.writeValueAsString(source));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erro convertendo Map<UUID,Integer> para JSON", e);
            }
        }
    }

    @ReadingConverter
    public static class JsonToMapConverter implements Converter<Json, Map<UUID, Integer>> {
        private final ObjectMapper objectMapper;

        public JsonToMapConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public Map<UUID, Integer> convert(Json source) {
            try {
                return objectMapper.readValue(source.asString(),
                        new TypeReference<Map<UUID, Integer>>() {});
            } catch (IOException e) {
                throw new RuntimeException("Erro convertendo JSON para Map<UUID,Integer>", e);
            }
        }
    }
}
//CREATE ALIAS IF NOT EXISTS uuid_generate_v4 FOR "java.util.UUID.randomUUID";