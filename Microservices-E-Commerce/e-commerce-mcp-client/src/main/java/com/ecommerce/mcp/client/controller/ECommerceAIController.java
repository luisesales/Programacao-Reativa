package com.ecommerce.mcp.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.mcp.client.services.ECommerceAIService;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
@RequestMapping("/chat")
public class ECommerceAIController {

    private static final Logger logger = LoggerFactory.getLogger(ECommerceAIController.class);

    private final ECommerceAIService ecommerceAIService;

    public ECommerceAIController(ECommerceAIService ecommerceAIService) {
        this.ecommerceAIService = ecommerceAIService;
    }

    @RateLimiter(name = "aiservice", fallbackMethod = "chatServiceRateFallback")
    @Retry(name= "retrychatservice", fallbackMethod = "chatServiceFallback")
    @Bulkhead(name= "bulkheadchatservice", fallbackMethod = "chatServiceFallback")
    @CircuitBreaker(name= "circuitchatservice", fallbackMethod = "chatServiceFallback")
    @GetMapping
    public ResponseEntity<String> chatService(@RequestParam("question") String prompt) {
        logger.info("Received chat request with prompt: '{}'", prompt);
        String answer = ecommerceAIService.getAnswer(prompt);
        logger.info("Returning AI answer for prompt: '{}'", prompt);
        return ResponseEntity.status(200).body(answer);
    }

    public ResponseEntity<String> chatServiceFallback() {
        logger.warn("Chat service fallback triggered. AI service is unavailable.");
        return ResponseEntity.status(503).body("O Serviço de Chat está indisponível");
    }

    public ResponseEntity<String> chatServiceRateFallback(Throwable t) {
        logger.warn("Rate limit exceeded for chat service. Error: {}", t.getMessage());
        return ResponseEntity.status(200).body("Rate limit exceeded: " + t.getMessage());
    }
}
