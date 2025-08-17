package com.bankai.mcp.client.model;

import java.util.List;
import java.util.Optional;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BankAI{
     @Autowired
     VectorStore vectorStore;
   
     public void add(List<Document> chuncks) {
         vectorStore.add(chuncks);
    }
    public List<Document> findClosestMatches(String query,int numberOfMatches) {
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(numberOfMatches)
            .build();
        return Optional
            .ofNullable(vectorStore.similaritySearch(request))
            .orElse(List.of());
            
        // return results.stream()
        //     .map((Document doc) -> doc.getText())
        //     .toList();
    }
    public Document findClosestMatch(String query) {
        List<Document> matches = findClosestMatches(query, 1);
        return matches.isEmpty() ? null : matches.get(0);            
    }

}
