package com.example.friendly_fishstick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class FriendlyFishstickApplication {

	public static void main(String[] args) {
		SpringApplication.run(FriendlyFishstickApplication.class, args);
	}

}
