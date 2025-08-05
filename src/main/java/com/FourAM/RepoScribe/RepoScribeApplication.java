package com.FourAM.RepoScribe;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
		"GITHUB_CLIENT_ID=dummy",
		"GITHUB_CLIENT_SECRET=dummy"
})
@SpringBootApplication
public class RepoScribeApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("GITHUB_CLIENT_ID", dotenv.get("GITHUB_CLIENT_ID"));
		System.setProperty("GITHUB_CLIENT_SECRET", dotenv.get("GITHUB_CLIENT_SECRET"));
		System.setProperty("MONGO_DB_URI", dotenv.get("MONGO_DB_URI"));
		System.setProperty("GEMINI_API_KEY", dotenv.get("GEMINI_API_KEY"));
		SpringApplication.run(RepoScribeApplication.class, args);
	}

}
