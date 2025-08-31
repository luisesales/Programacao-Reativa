package com.ecommerce.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ecommerce.mcpserver.model.Order;
import com.ecommerce.mcpserver.service.OrderAIService;

@Component
public class OrderAITools {

    public String orderProduct(
        @ToolParam(description = """
                                    The order to be placed with:
                                    productId a Long that represents the id of the product to be ordered, 
                                    quantity a int that represents the number of items to be ordered, 
                                    name a String of up to 30 characters representing the name of the customer placing the order,                                     
                                """
                    ) Order order
    ) {
        return orderAiService.orderProduct(order);
    }

}
