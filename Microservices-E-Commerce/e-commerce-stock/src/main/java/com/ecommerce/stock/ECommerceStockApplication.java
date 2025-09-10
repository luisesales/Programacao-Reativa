package com.ecommerce.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceStockApplication.class, args);
	}

}
