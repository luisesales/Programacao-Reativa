package com.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceOrderApplication.class, args);
	}

}
