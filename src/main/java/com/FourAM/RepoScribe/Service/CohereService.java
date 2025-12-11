package com.FourAM.RepoScribe.Service;

import com.FourAM.RepoScribe.Properties.CohereProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class CohereService {

    private final WebClient webClient;

    public CohereService(CohereProperties properties) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.cohere.com/v2")
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String testConnection() {
        return generateDocumentation("Hello Cohere, just testing connection.");
    }

    public String generateDocumentation(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "command-a-03-2025",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("message")) {

                Map<String, Object> message =
                        (Map<String, Object>) response.get("message");

                // content is ALWAYS a List in Cohere v2 API
                List<Map<String, Object>> contentList =
                        (List<Map<String, Object>>) message.get("content");

                if (contentList != null && !contentList.isEmpty()) {
                    Map<String, Object> firstBlock = contentList.get(0);

                    // extract the actual generated text
                    return (String) firstBlock.get("text");
                }
            }

            return "⚠️ No response from Cohere";

        } catch (Exception e) {
            return "❌ Error calling Cohere API: " + e.toString();
        }
    }
}
