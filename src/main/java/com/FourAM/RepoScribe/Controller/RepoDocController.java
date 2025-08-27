package com.FourAM.RepoScribe.Controller;

import com.FourAM.RepoScribe.Model.User;
import com.FourAM.RepoScribe.Repository.UserRepository;
import com.FourAM.RepoScribe.Service.CohereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RepoDocController {

    private final CohereService cohereService;
    private final UserRepository userRepository;

    @Autowired
    public RepoDocController(CohereService cohereService, UserRepository userRepository) {
        this.cohereService = cohereService;
        this.userRepository = userRepository;
    }

    @GetMapping("/testAI")
    public String testConnection() {
        return cohereService.testConnection();
    }

    @PostMapping("/generateDoc")
    public ResponseEntity<?> generateDoc(@RequestParam String accessToken,
                                         @RequestBody User userRequest) {
        try {
            User userToSave = new User();
            userToSave.setRepoLink(userRequest.getRepoLink());
            userToSave.setContainsAPI(userRequest.isContainsAPI());
            userRepository.save(userToSave);

            String[] parts = userRequest.getRepoLink()
                    .replace("https://github.com/", "")
                    .split("/");
            if (parts.length < 2) {
                return ResponseEntity.badRequest().body("Invalid repo URL");
            }
            String owner = parts[0];
            String repo = parts[1];

            StringBuilder repoCode = new StringBuilder();
            int[] count = {0};
            fetchFilesRecursively(owner, repo, "", accessToken, repoCode, count);

            String repoPrompt = """
            You are a senior technical writer.
            TASK:
            1. Read the provided repository code.
            2. Generate ONLY repository documentation (purpose, tech stack, features, setup).
            3. Do NOT include API documentation.
            4. Do NOT guess. If details are missing, write "Not provided in repository".
            5. Output in Markdown format.

            PROJECT NAME: %s

            REPOSITORY CONTENT:
            %s
            """.formatted(repo, repoCode);

            String repoDoc = cohereService.generateDocumentation(repoPrompt);

            String apiDoc = "";
            if (userRequest.isContainsAPI()) {
                String apiPrompt = """
                You are a senior technical writer.
                TASK:
                1. Read the provided repository code.
                2. Generate ONLY API documentation (endpoints, request/response examples).
                3. Do NOT include repository summary or setup.
                4. Do NOT guess. If details are missing, write "Not provided in repository".
                5. Output in Markdown format.

                PROJECT NAME: %s

                REPOSITORY CONTENT:
                %s
                """.formatted(repo, repoCode);

                apiDoc = cohereService.generateDocumentation(apiPrompt);
            }

            return ResponseEntity.ok(Map.of(
                    "repo", userRequest.getRepoLink(),
                    "repoDoc", repoDoc,
                    "apiDoc", apiDoc
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    private void fetchFilesRecursively(String owner, String repo, String path, String accessToken, StringBuilder repoCode, int[] count) {
        WebClient githubApi = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();

        List<Map<String, Object>> files = githubApi.get()
                .uri("/repos/{owner}/{repo}/contents/{path}", owner, repo, path)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (files == null) return;

        for (Map<String, Object> file : files) {
            String type = (String) file.get("type");
            String filePath = (String) file.get("path");

            if ("file".equals(type) && count[0] < 20) {
                String downloadUrl = (String) file.get("download_url");
                String content = WebClient.create()
                        .get()
                        .uri(downloadUrl)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                repoCode.append("\n\nFile: ").append(filePath).append("\n").append(content);
                count[0]++;
            } else if ("dir".equals(type)) {
                fetchFilesRecursively(owner, repo, filePath, accessToken, repoCode, count);
            }
        }
    }
}
