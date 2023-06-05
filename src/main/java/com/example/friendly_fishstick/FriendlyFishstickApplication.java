package com.example.friendly_fishstick;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.UUID;

@SpringBootApplication
@EnableMongoRepositories
public class FriendlyFishstickApplication {

	private static final Logger logger = LoggerFactory.getLogger(FriendlyFishstickApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FriendlyFishstickApplication.class, args);
	}


	@Bean
	ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener(OrderRepository repo) {
		repo.deleteAll();

		repo.save(new Order(UUID.randomUUID().toString(), "name_1", "customer"));
		repo.save(new Order(UUID.randomUUID().toString(), "name_2", "customer"));
		repo.save(new Order(UUID.randomUUID().toString(), "name_3", "customer_1"));
		repo.save(new Order(UUID.randomUUID().toString(), "name_4", "customer_2"));
		repo.save(new Order(UUID.randomUUID().toString(), "name_5", "admin"));
		repo.save(new Order(UUID.randomUUID().toString(), "name_6", "admin"));


		logger.info("Initialized database with orders:");
		return event -> repo.findAll().forEach(order -> logger.info(String.valueOf(order)));
	}
}
