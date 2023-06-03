package com.example.friendly_fishstick;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    public List<Order> findByName(String name);
}
