package com.FourAM.RepoScribe;

import com.FourAM.RepoScribe.Properties.CohereProperties;
import com.FourAM.RepoScribe.Properties.GitHubProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({GitHubProperties.class,CohereProperties.class})
@SpringBootApplication
public class RepoScribeApplication {
	public static void main(String[] args) {
		SpringApplication.run(RepoScribeApplication.class, args);
	}
}

