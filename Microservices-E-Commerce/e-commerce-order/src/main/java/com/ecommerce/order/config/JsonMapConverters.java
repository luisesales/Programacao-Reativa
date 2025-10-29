package com.ecommerce.order.config;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.r2dbc.postgresql.codec.Json;

public class JsonMapConverters {

    @ReadingConverter
    public static class JsonToMapConverter implements Converter<Json, Map<UUID, Integer>> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public Map<UUID, Integer> convert(Json source) {
            try {
                return mapper.readValue(source.asString(), new TypeReference<HashMap<UUID, Integer>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Error converting JSON to Map", e);
            }
        }
    }

    @WritingConverter
    public static class MapToJsonConverter implements Converter<Map<UUID, Integer>, Json> {
        private final ObjectMapper mapper = new ObjectMapper();

        @Override
        public Json convert(Map<UUID, Integer> source) {
            try {
                return Json.of(mapper.writeValueAsString(source));
            } catch (Exception e) {
                throw new RuntimeException("Error converting Map to JSON", e);
            }
        }
    }
}