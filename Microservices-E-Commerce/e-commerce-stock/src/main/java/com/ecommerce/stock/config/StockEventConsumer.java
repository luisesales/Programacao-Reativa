package com.ecommerce.stock.config;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.ecommerce.stock.event.stock.StockRequested;
import com.ecommerce.stock.service.ProductService;

@Service
public class StockEventConsumer {
     private final ProductService productService;

    public StockEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<StockRequested> onStockRequested() {
        return event -> productService.handle(event);
    }
}
