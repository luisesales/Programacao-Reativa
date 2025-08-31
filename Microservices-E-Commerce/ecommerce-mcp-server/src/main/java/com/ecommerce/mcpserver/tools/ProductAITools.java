package com.ecommerce.mcpserver.tools;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.ecommerce.mcpserver.model.Order;
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

    

    @Tool(
        name = "criarContanoBanco",
        description = "Cria uma conta no banco dado que seja fornecido um nome para a conta e uma conta com esse nome já não exista"
    )
    public Account criarContanoBanco(
        @ToolParam(description = "@ToolParameter(name = \"id\", description = \"Identificador único da conta\", required = false)\n" + //
                        "    private Long id;\n" + //
                        "\n" + //
                        "    @ToolParameter(name = \"ecommerceId\", description = \"Identificador do banco associado à conta\", required = true)\n" + //
                        "    private Long ecommerceId;\n" + //
                        "\n" + //
                        "    @ToolParameter(name = \"name\", description = \"Nome do titular da conta\", required = true)\n" + //
                        "    private String name;\n" + //
                        "\n" + //
                        "    @ToolParameter(name = \"balance\", description = \"Saldo atual da conta\", required = false)\n" + //
                        "    private double balance;\n" + //
                        "\n" + //
                        "    @ToolParameter(name = \"isActive\", description = \"Indica se a conta está ativa\", required = false)\n" + //
                        "    private boolean isActive;") Account account
    ) {
        System.out.println("Entrei na Tool create Account do MCP Server");
        return ecommerceAiService.createAccount(account);
    }

    @Tool(
        name = "depositarNaContaNoBanco",
        description = "Deposita um valor x conta no banco dado que seja fornecido um valor numérico para depositar na conta e um id long considerando que uma conta com esse id já exista"
    )
    public Account depositarNaContaNoBanco(
        @ToolParam(description = "É um double que permite até dois caracteres flutuantes e um id em long") double value, Long accountId
    ) {
        System.out.println("Entrei na Tool deposit Account do MCP Server");
        return ecommerceAiService.depositAccount(accountId, value);
    }

    @Tool(
        name = "sacarNaContaNoBanco",
        description = "Saca um valor x conta no banco dado que seja fornecido um valor numérico para depositar na conta e um id long considerando que uma conta com esse id já exista"
    )
    public Account sacarNaContaNoBanco(
        @ToolParam(description = "É um double que permite até dois caracteres flutuantes e um id em long") double value, Long accountId
    ) {
        System.out.println("Entrei na Tool draw Account do MCP Server");
        return ecommerceAiService.drawAccount(accountId, value);
    }

    @Tool(
        name = "balancoNaContaNoBanco",
        description = "Verifica o saldo de uma conta no banco dado que seja fornecido um long id e uma conta com esse id já exista"
    )
    public Double balancoNaContaNoBanco(
        @ToolParam(description = "É uma id em long") Long accountId
    ) {
        System.out.println("Entrei na Tool balance Account do MCP Server");
        return ecommerceAiService.balanceAccount(accountId);
    }
}
