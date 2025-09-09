package com.ecommerce.order.exchange;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@Component
@HttpExchange(url = "/chat")
public interface AiHttpInterface {    

    @GetExchange
    @CircuitBreaker(name= "cbMcpClientChat")
    @Retry(name= "rtMcpClientChat")
    @Bulkhead(name= "bhMcpClientChat")
    @RateLimiter(name = "rlMcpClientChat")
    public ResponseEntity<String> promptAi(@RequestParam String question);
}
