package com.ecommerce.mcp.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.mcp.client.services.ECommerceAIService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/chat")
public class ECommerceAIController {

    private static final Logger logger = LoggerFactory.getLogger(ECommerceAIController.class);

    private final ECommerceAIService ecommerceAIService;

    public ECommerceAIController(ECommerceAIService ecommerceAIService) {
        this.ecommerceAIService = ecommerceAIService;
    }

    @RateLimiter(name = "rlMcpClientChat", fallbackMethod="chatServiceRateFallback")
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatService(@RequestParam("question") String prompt) {
        logger.info("Received chat request with prompt: '{}'", prompt);
        return ecommerceAIService.getAnswer(prompt)
            .doOnError(e -> {
                logger.error("Error processing chat request", e);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error processing chat request: " + e.getMessage(), e);
            });
    }

    public Flux<String> chatServiceRateFallback(Throwable t) {
        logger.warn("Rate limit exceeded for chat service. Error: {}", t.getMessage());
        return Flux.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Rate limit exceeded: " + t.getMessage()));
    }
}
