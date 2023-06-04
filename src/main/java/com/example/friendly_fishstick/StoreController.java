package com.example.friendly_fishstick;

import com.mongodb.MongoWriteException;
import jakarta.annotation.security.RolesAllowed;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/store")
public class StoreController {

    private final OrderRepository orderRepository;

    public StoreController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public void createOrder(@RequestBody OrderDTO orderDto, Authentication auth) {
        orderRepository.insert(new Order(UUID.randomUUID().toString(), orderDto.name(), auth.getName()));
    }

    @GetMapping
    public List<Order> listOrders(@RequestParam(required = false) String customerName, Authentication auth) {
        var auths = auth.getAuthorities().stream().toList();
        if (Objects.isNull(customerName)) {
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"))) {
                throw new UnauthorizedOperationException();
            }
        } else {
            if (customerName.equals(auth.getName())) {
                return orderRepository.findByCreatedBy(customerName);
            } else {
                if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return orderRepository.findByCreatedBy(customerName);
                }
                throw new UnauthorizedOperationException();
            }
        }
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return orderRepository.findAll();
        }
        return List.of();
    }

    @DeleteMapping("{id}")
    public void removeOrder(@PathVariable("id") String orderId) {
        this.orderRepository.deleteById(orderId);
    }

    @ResponseBody
    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    void unauthorizedOperationException(UnauthorizedOperationException ex) {}
}
