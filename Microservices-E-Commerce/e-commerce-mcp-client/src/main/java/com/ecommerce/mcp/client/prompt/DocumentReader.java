package com.ecommerce.mcp.client.prompt;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentReader {    

    DocumentReader() {        
    }

    public List<Document> loadText() {        
        String exemplo = """
        Este é um regulamento fictício utilizado apenas para testes da aplicação.
        usuário pode Criar e Deletar Produtos no Estoque e realizar compras utilizando o serviço de IA.
        """;

        Document doc = new Document(exemplo);
        return List.of(doc);
    }
}
