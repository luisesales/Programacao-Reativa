package com.ecommerce.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.ecommerce.mcp.server.tools.OrderAITools;
import com.ecommerce.mcp.server.tools.ProductAITools;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceMcpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider productAiTools(ProductAITools productAITools) {		
    return MethodToolCallbackProvider.builder()
        .toolObjects(productAITools)
        .build();
	}

	@Bean
	public ToolCallbackProvider orderAiTools(OrderAITools orderAITools) {		
    return MethodToolCallbackProvider.builder()
        .toolObjects(orderAITools)
        .build();
	}
}
