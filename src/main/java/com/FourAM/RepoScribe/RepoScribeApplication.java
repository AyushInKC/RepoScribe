package com.FourAM.RepoScribe;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

@TestPropertySource(properties = {
		"GITHUB_CLIENT_ID=dummy",
		"GITHUB_CLIENT_SECRET=dummy"
})
@SpringBootApplication
public class RepoScribeApplication {

	public static void main(String[] args) {

		// Only load .env if it exists (local dev)
		File envFile = new File(".env");
		if (envFile.exists()) {
			Dotenv dotenv = Dotenv.load();
			dotenv.entries().forEach(entry -> {
				// Only set property if not already defined in environment
				if (System.getenv(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
			System.out.println("Loaded environment variables from .env file (local development mode).");
		} else {
			System.out.println("No .env file found — using environment variables from system/cloud.");
		}

		SpringApplication.run(RepoScribeApplication.class, args);
	}
}
