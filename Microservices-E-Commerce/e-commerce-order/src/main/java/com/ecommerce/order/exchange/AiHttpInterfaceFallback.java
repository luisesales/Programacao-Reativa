package com.ecommerce.order.exchange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory; 
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Component
public class AiHttpInterfaceFallback implements AiHttpInterface {

    private static final Logger logger = LoggerFactory.getLogger(AiHttpInterfaceFallback.class);

    @Override
    public ResponseEntity<String> promptAi(@RequestParam String question) {
        logger.warn("Fallback added to promptAi for question: {}. Returning error message.", question);
        return ResponseEntity.status(503).body("The AI service is currently unavailable.");
    }
}
