package com.ecommerce.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.ecommerce.order.exchange.ProductHttpInterface;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceOrderApplication.class, args);
	}	
}


