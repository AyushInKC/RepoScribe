package com.FourAM.RepoScribe.Controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

@RestController
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
    public void login(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=repo&state=" + System.currentTimeMillis();

        response.sendRedirect(redirectUrl);
    }

    /**
     * Step 2: Handle GitHub callback and exchange code for access token
     */
    @GetMapping("/auth/github/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
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

        // Instead of fetching repos, just return token for now
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }


}
