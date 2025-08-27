package com.FourAM.RepoScribe;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

@SpringBootApplication
public class RepoScribeApplication {

	public static void main(String[] args) {

		SpringApplication.run(RepoScribeApplication.class, args);
	}
}
