package com.ecommerce.mcp.client.interfaces;

import reactor.core.publisher.Flux;

public interface ChatServiceAi {
    Flux<String> getAnswer(String question);
}
