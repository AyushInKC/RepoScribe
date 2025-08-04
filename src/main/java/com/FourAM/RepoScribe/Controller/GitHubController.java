package com.FourAM.RepoScribe.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Controller
public class GitHubController {

    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient;

    public GitHubController(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://github.com").build();
    }

    /**
     * Step 1: Redirect user to GitHub login page
     */
    @GetMapping("/auth/github/login")
    public String login() {
        return "redirect:https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=repo&state=" + System.currentTimeMillis();
    }

    /**
     * Step 2: Handle GitHub callback and exchange code for access token
     */
    @GetMapping("/auth/github/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        // 1. Exchange code for token
        Map<String, Object> tokenResponse = webClient.post()
                .uri("/login/oauth/access_token")
                .header("Accept", "application/json")
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        String accessToken = (String) tokenResponse.get("access_token");

        // 2. Use token to get repos
        WebClient githubApiClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();

        Object repos = githubApiClient.get()
                .uri("/user/repos?per_page=100") // get up to 100 repos
                .retrieve()
                .bodyToMono(Object.class)
                .block();

        return ResponseEntity.ok(repos);
    }

}
