package com.example.friendly_fishstick;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stats")
public record Order(String id, String name, String createdBy) {
}
