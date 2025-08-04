package com.FourAM.RepoScribe.Controller;

import com.FourAM.RepoScribe.Model.User;
import com.FourAM.RepoScribe.Repository.UserRepository;
import com.FourAM.RepoScribe.Service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RepoDocController {

    private final GeminiService geminiService;
    private final UserRepository userRepository;

    @Autowired
    public RepoDocController(GeminiService geminiService, UserRepository userRepository) {
        this.geminiService = geminiService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generateDoc")
    public ResponseEntity<?> generateDoc(@RequestParam String accessToken,
                                         @RequestBody User userRequest) {
        try {
            // 1️⃣ Save user input to DB
            userRepository.save(userRequest);

            // 2️⃣ Parse owner/repo
            String[] parts = userRequest.getRepoLink()
                    .replace("https://github.com/", "")
                    .split("/");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body("Invalid repo URL");
            }
            String owner = parts[0];
            String repo = parts[1];

            // 3️⃣ Fetch file list
            WebClient githubApi = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .defaultHeader("Authorization", "Bearer " + accessToken)
                    .defaultHeader("Accept", "application/vnd.github+json")
                    .build();

            List<Map<String, Object>> files = githubApi.get()
                    .uri("/repos/{owner}/{repo}/contents", owner, repo)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body("No files found in repository.");
            }

            // 4️⃣ Get first few file contents
            StringBuilder repoCode = new StringBuilder();
            int count = 0;
            for (Map<String, Object> file : files) {
                if ("file".equals(file.get("type")) && count < 5) {
                    String downloadUrl = (String) file.get("download_url");
                    String content = WebClient.create()
                            .get()
                            .uri(downloadUrl)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                    repoCode.append("\n\nFile: ").append(file.get("name"))
                            .append("\n").append(content);
                    count++;
                }
            }

            // 5️⃣ Create AI prompt based on API flag
            String prompt;
            if (userRequest.isContainsAPI()) {
                prompt = """
                        You are a documentation generator.
                        Read the following codebase and generate:
                        1. A summary of the project.
                        2. API documentation (methods, request, response).
                        
                        Code:
                        """ + repoCode;
            } else {
                prompt = """
                        You are a documentation generator.
                        Read the following codebase and generate:
                        1. A summary of the project.
                        
                        Code:
                        """ + repoCode;
            }

            // 6️⃣ Get AI documentation
            String documentation = geminiService.generateDocumentation(prompt);

            // 7️⃣ Return response
            return ResponseEntity.ok(Map.of(
                    "repo", userRequest.getRepoLink(),
                    "documentation", documentation
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
