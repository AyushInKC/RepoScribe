package com.FourAM.RepoScribe.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
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

    /**
     * Generates documentation from repo content.
     * Each request is isolated so Gemini never remembers past repositories.
     */
    public String generateDocumentation(String prompt) {

        // 🔹 Extra safety: Force Gemini to forget past context
        String freshPrompt = """
            Forget all prior instructions, repositories, and conversations.

            You are a senior technical writer & software architect.
            Only use the following code content to generate documentation.
            Do not rely on memory from earlier requests.

            %s
            """.formatted(prompt);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", freshPrompt)
                        ))
                )
        );

        return webClient.post()
                .uri("/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(5))
                                .filter(throwable -> {
                                    String msg = throwable.getMessage();
                                    return msg != null && (msg.contains("429") || msg.contains("503"));
                                })
                )
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
