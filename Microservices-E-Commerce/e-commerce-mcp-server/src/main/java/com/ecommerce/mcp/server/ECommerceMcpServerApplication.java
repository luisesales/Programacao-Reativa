package com.ecommerce.mcp.server;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.ecommerce.mcp.server.exchange.OrderHttpInterface;
import com.ecommerce.mcp.server.exchange.ProductHttpInterface;
import com.ecommerce.mcp.server.tools.OrderAITools;
import com.ecommerce.mcp.server.tools.ProductAITools;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
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
	public ProductHttpInterface productHttpInterface(
		@Value("http://localhost:8085/ecommerce-stock") String baseUrl
	) {
		RestClient productsClient = RestClient.builder()
			.baseUrl(baseUrl)
			.build();

		System.out.println(baseUrl);

		HttpServiceProxyFactory factory = HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(productsClient))
			.build();

		return factory.createClient(ProductHttpInterface.class);
	}

	@Bean
	public ToolCallbackProvider orderAiTools(OrderAITools orderAITools) {		
    return MethodToolCallbackProvider.builder()
        .toolObjects(orderAITools)
        .build();
	}

	@Bean
	public OrderHttpInterface orderHttpInterface(
		@Value("http://localhost:8085/ecommerce-order") String baseUrl
	) {
		RestClient ordersClient = RestClient.builder()
			.baseUrl(baseUrl)
			.build();

		System.out.println(baseUrl);

		HttpServiceProxyFactory factory = HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(ordersClient))
			.build();

		return factory.createClient(OrderHttpInterface.class);
	}
}
