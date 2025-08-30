package com.FourAM.RepoScribe.Service;

import com.FourAM.RepoScribe.Properties.CohereProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class CohereService {

    private final WebClient webClient;

    public CohereService(CohereProperties properties) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.cohere.ai/v1")
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String testConnection() {
        String prompt = "Hello Cohere, just testing connection.";
        return generateDocumentation(prompt);
    }

    public String generateDocumentation(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "command-r-plus",   // ✅ best for doc generation
                    "prompt", prompt,
                    "max_tokens", 800
            );

            Map<String, Object> response = webClient.post()
                    .uri("/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("generations")) {
                var generations = (java.util.List<Map<String, Object>>) response.get("generations");
                if (!generations.isEmpty()) {
                    return (String) generations.get(0).get("text");
                }
            }
            return "⚠️ No response from Cohere";
        } catch (Exception e) {
            return "❌ Error calling Cohere API: " + e.getMessage();
        }
    }
}
