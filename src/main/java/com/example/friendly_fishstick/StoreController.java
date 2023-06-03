package com.example.friendly_fishstick;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final OrderRepository orderRepository;

    public StoreController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping("{id}")
    public void createOrder(@RequestBody Order order) {
        orderRepository.insert(order);
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(required = false) String username) {
        if (username != null ) {
            return orderRepository.findByName(username);
        }
        return orderRepository.findAll();
    }

    @DeleteMapping("{id}")
    public void removeOrder(@PathVariable("id") String orderId) {
        this.orderRepository.deleteById(orderId);
    }
}
