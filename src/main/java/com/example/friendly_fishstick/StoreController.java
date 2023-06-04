package com.example.friendly_fishstick;

import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final OrderRepository orderRepository;

    public StoreController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public void createOrder(@RequestBody OrderDTO orderDto) {
        orderRepository.insert(new Order(UUID.randomUUID().toString(), orderDto.name(), orderDto.createdBy()));
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(required = false) String customer) {
        if (customer != null) {
            return orderRepository.findByCreatedBy(customer);
        }
        return orderRepository.findAll();
    }

    @DeleteMapping("{id}")
    public void removeOrder(@PathVariable("id") String orderId) {
        this.orderRepository.deleteById(orderId);
    }

    @ResponseBody
    @ExceptionHandler(MongoWriteException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    String orderDuplicated(MongoWriteException ex) {
        return "Order with provided Id already exists";
    }
}
