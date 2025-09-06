package com.FourAM.RepoScribe.Controller;

import com.FourAM.RepoScribe.Properties.GitHubProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
public class GitHubController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final GitHubProperties githubProperties;

    public GitHubController(GitHubProperties githubProperties) {
        this.githubProperties = githubProperties;
    }

    // GitHub login endpoint
    @GetMapping("/auth/github/login")
    public void login(HttpServletResponse response) throws IOException {
        if (githubProperties.getClientId() == null || githubProperties.getRedirectUri() == null) {
            throw new IllegalStateException("❌ GitHub OAuth properties not loaded properly.");
        }

        String redirectUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + githubProperties.getClientId() +
                "&redirect_uri=" + githubProperties.getRedirectUri() +
                "&scope=repo&state=" + System.currentTimeMillis();

        response.sendRedirect(redirectUrl);
    }

    // GitHub callback endpoint
    @GetMapping("/auth/github/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");  // GitHub requires this

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubProperties.getClientId());
        body.add("client_secret", githubProperties.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", githubProperties.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                entity,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }

}
