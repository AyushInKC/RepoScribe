package com.FourAM.RepoScribe.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://generativelanguage.googleapis.com/v1")
                .build();
    }

    public String generateDocumentation(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        return webClient.post()
                .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey) // updated model
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    try {
                        var candidates = (List<Map<String, Object>>) response.get("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            var content = (Map<String, Object>) candidates.get(0).get("content");
                            var parts = (List<Map<String, Object>>) content.get("parts");
                            return (String) parts.get(0).get("text");
                        }
                    } catch (Exception e) {
                        return "Failed to parse Gemini API response: " + e.getMessage();
                    }
                    return "No documentation generated.";
                })
                .block();
    }
}
