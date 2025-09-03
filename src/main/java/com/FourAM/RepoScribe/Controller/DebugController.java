package com.FourAM.RepoScribe.Controller;

import com.FourAM.RepoScribe.Properties.GitHubProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class DebugController {
    private final GitHubProperties githubProperties;

    public DebugController(GitHubProperties githubProperties) {
        this.githubProperties = githubProperties;
    }

    @GetMapping("/debug")
    public String debug() {
        return "ClientId=" + githubProperties.getClientId()
                + ", RedirectUri=" + githubProperties.getRedirectUri();
    }
}
