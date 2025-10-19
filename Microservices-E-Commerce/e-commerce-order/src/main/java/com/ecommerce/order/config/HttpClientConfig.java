package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.ecommerce.order.exchange.AiHttpInterface;
import com.ecommerce.order.exchange.ProductHttpInterface;

@Configuration
public class HttpClientConfig {

    @Bean
    public ProductHttpInterface productHttpInterface(WebClient.Builder webClientBuilder,
                                                    @Value("http://localhost:8085/e-commerce-stock") String gatewayBaseUrl) {

        WebClient webClient = webClientBuilder
                .baseUrl(gatewayBaseUrl)   
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(ProductHttpInterface.class);
    }

    @Bean
    public AiHttpInterface aiHttpInterface(WebClient.Builder webClientBuilder,
                                                    @Value("http://localhost:8085/e-commerce-mcp-client") String gatewayBaseUrl) {

        WebClient webClient = webClientBuilder
                .baseUrl(gatewayBaseUrl)   
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(AiHttpInterface.class);
    }
}
