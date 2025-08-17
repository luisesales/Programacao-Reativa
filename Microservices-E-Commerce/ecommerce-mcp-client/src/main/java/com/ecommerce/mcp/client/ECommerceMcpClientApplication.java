package com.ecommerce.mcp.client;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import com.ecommerce.mcp.client.model.ECommerceAI;
import com.ecommerce.mcp.client.prompt.DocumentReader;

import io.modelcontextprotocol.client.McpSyncClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ECommerceMcpClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ECommerceMcpClientApplication.class, args);
	}

	@Bean
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		return SimpleVectorStore.builder(embeddingModel).build();
	}

	@Bean
    public CommandLineRunner initVectorStore(DocumentReader documentReader, ECommerceAI bankAI) {
        return args -> {
            System.out.println("Iniciando a ingestão de documentos para o VectorStore...");
            List<Document> loadedDocuments = documentReader.loadText();

            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> chunks = textSplitter.apply(loadedDocuments);

            bankAI.add(chunks);
            System.out.println("Documentos ingeridos no VectorStore com sucesso!");
        };
	}
}
