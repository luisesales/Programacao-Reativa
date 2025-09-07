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

    @RateLimiter(name = "rlMcpClientChat", fallbackMethod="chatServiceRateFallback")
    @GetMapping
    public ResponseEntity<String> chatService(@RequestParam("question") String prompt) {
        logger.info("Received chat request with prompt: '{}'", prompt);
        String answer = ecommerceAIService.getAnswer(prompt);
        logger.info("Returning AI answer for prompt: '{}'", prompt);
        return ResponseEntity.status(200).body(answer);
    }

    public ResponseEntity<String> chatServiceRateFallback(Throwable t) {
        logger.warn("Rate limit exceeded for chat service. Error: {}", t.getMessage());
        return ResponseEntity.status(503).body("Rate limit exceeded: " + t.getMessage());
    }
}
