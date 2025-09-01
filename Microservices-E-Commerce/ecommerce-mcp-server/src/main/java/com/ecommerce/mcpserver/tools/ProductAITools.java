package com.ecommerce.mcpserver.tools;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ecommerce.mcpserver.model.Product;
import com.ecommerce.mcpserver.service.ProductAIService;

@Component
public class ProductAITools {
    
    private final ProductAIService productAiService;

    public ProductAITools(ProductAIService productAiService) {
        this.productAiService = productAiService;
    }

    @Tool(
        name = "createProduct",
        description = "Creates a new product given a name, description, and price and category"
    )
    public Product createProduct(
        @ToolParam(description = """
                                    The product to be created with:
                                    name a String of up to 15 characters, 
                                    description a String of up to 30 characters, 
                                    price a double with a range of 2 decimal numbers, 
                                    stockQuantity a int that if not given starts with zero and 
                                    category a String of up to 15 characters
                                """
                    ) Product product
    ) {
        return productAiService.createProduct(product);
    }

    @Tool(
        name = "getAllProducts",
        description = "Retrieves all products available in the inventory"
    )
    public List<Product> getAllProducts() {
        return productAiService.getAllProducts();
    }
    @Tool(
        name = "getProductById",
        description = "Fetches a product by its unique identifier"
    )
    public Optional<Product> getProductById(
        @ToolParam(description = "It's a Long id") Long id
    ) {
        return productAiService.getProductById(id);
    }

}
