package com.example.shop_recruitment;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stats")
public record Order(String id, String name, String createdBy) {
}
