package com.altspot.local;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class LocalMusicPlayerApplication {

	private final Environment env;

	public LocalMusicPlayerApplication(Environment env) {
		this.env = env;
	}

	public static void main(String[] args) {
		SpringApplication.run(LocalMusicPlayerApplication.class, args);
	}

	@PostConstruct
	void checkProfile() {
		System.out.println(
				"Active profiles: " + String.join(", ", env.getActiveProfiles())
		);
	}
}
