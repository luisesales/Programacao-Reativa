package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication(scanBasePackages = "com.ecommerce.order")
@EnableDiscoveryClient
@EnableR2dbcRepositories
public class ECommerceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceOrderApplication.class, args);
	}	
}


