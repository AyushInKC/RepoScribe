package com.FourAM.RepoScribe.Controller;

import com.FourAM.RepoScribe.Properties.GitHubProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
public class GitHubController {

    private final RestTemplate restTemplate;
    private final GitHubProperties githubProperties;

    public GitHubController(RestTemplate restTemplate, GitHubProperties githubProperties) {
        this.restTemplate = restTemplate;
        this.githubProperties = githubProperties;
    }



    // GitHub login endpoint
    @GetMapping("/auth/github/login")
    public void login(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://github.com/login/oauth/authorize" +
                "?client_id=" + githubProperties.getClientId() +
                "&redirect_uri=" + githubProperties.getRedirectUri() +
                "&scope=repo&state=" + System.currentTimeMillis();

        response.sendRedirect(redirectUrl);
    }

    // GitHub callback endpoint
    @GetMapping("/auth/github/callback")
    public ResponseEntity<?> callback(@RequestParam String code) {
        Map<String, String> request = Map.of(
                "client_id", githubProperties.getClientId(),
                "client_secret", githubProperties.getClientSecret(),
                "code", code,
                "redirect_uri", githubProperties.getRedirectUri()
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token?accept=json",
                request,
                Map.class
        );

        return ResponseEntity.ok(response.getBody());
    }
}
