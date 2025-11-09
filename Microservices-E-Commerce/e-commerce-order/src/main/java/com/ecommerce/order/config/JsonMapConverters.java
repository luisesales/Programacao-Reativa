// package com.ecommerce.order.config;

// import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import io.r2dbc.postgresql.codec.Json;
// import org.springframework.core.convert.converter.Converter;
// import org.springframework.data.convert.ReadingConverter;
// import org.springframework.data.convert.WritingConverter;

// import java.nio.charset.StandardCharsets;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.UUID;

// public class JsonMapConverters {

//     @ReadingConverter
//     public static class JsonToMapConverter implements Converter<Object, Map<UUID, Integer>> {

//         private final ObjectMapper mapper;

//         public JsonToMapConverter(ObjectMapper mapper) {
//             this.mapper = mapper;
//         }

//         @Override
//         public Map<UUID, Integer> convert(Object source) {
//             try {
//                 String json;

//                 if (source instanceof Json j) {
//                     json = j.asString();
//                 } else if (source instanceof CharSequence cs) {
//                     json = cs.toString();
//                 } else if (source instanceof byte[] bytes) {
//                     json = new String(bytes, StandardCharsets.UTF_8);
//                 } else if (source != null && source.getClass().getName().equals("io.r2dbc.postgresql.codec.Json$JsonByteArrayInput")) {
//                     var method = source.getClass().getDeclaredMethod("asString");
//                     method.setAccessible(true);
//                     json = (String) method.invoke(source);
//                 } else {
//                     json = String.valueOf(source);
//                 }

//                 return mapper.readValue(json, new TypeReference<HashMap<UUID, Integer>>() {});
//             } catch (Exception e) {
//                 throw new IllegalStateException("Error converting JSONB to Map<UUID,Integer>: " + source, e);
//             }
//         }
//     }

//     @WritingConverter
//     public static class MapToJsonConverter implements Converter<Map<UUID, Integer>, Json> {

//         private final ObjectMapper mapper;

//         public MapToJsonConverter(ObjectMapper mapper) {
//             this.mapper = mapper;
//         }

//         @Override
//         public Json convert(Map<UUID, Integer> source) {
//             System.out.println("✅ JsonToMapConverter invoked for: " + source.getClass());
//             try {
//                 return Json.of(mapper.writeValueAsString(source));
//             } catch (Exception e) {
//                 throw new IllegalStateException("Error converting Map<UUID,Integer> to JSONB", e);
//             }
//         }
//     }
// }
