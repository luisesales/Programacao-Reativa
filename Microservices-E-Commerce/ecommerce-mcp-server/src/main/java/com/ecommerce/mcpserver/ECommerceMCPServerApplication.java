package com.bankai.mcpserver;

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

import com.bankai.mcpserver.tools.BankAITools;
import com.bankai.mcpserver.http.AccountHttpInterface;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BankmcpserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankmcpserverApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider aiTools(BankAITools bankAITools) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(bankAITools)
        .build();
	}

	@Bean
	public AccountHttpInterface produtosHttpInterface(
		@Value("http://localhost:8085/bankai-stock") String baseUrl
	) {
		RestClient produtosClient = RestClient.builder()
			.baseUrl(baseUrl)
			.build();

		System.out.println(baseUrl);

		HttpServiceProxyFactory factory = HttpServiceProxyFactory
			.builderFor(RestClientAdapter.create(produtosClient))
			.build();

		return factory.createClient(AccountHttpInterface.class);
	}
}
