package com.example.friendly_fishstick;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.example.friendly_fishstick.SecurityConfig.ROLE_PREFIX;

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
        if (checkAuthoritiesRole(auth.getAuthorities(), Roles.CUSTOMER)) {
            if (Objects.isNull(customerName) || !customerName.equals(auth.getName())) {
                throw new UnauthorizedOperationException();
            }
            return orderRepository.findByCreatedBy(customerName);
        }
        if (checkAuthoritiesRole(auth.getAuthorities(), Roles.ADMIN)) {
            if (Objects.nonNull(customerName)) {
                return orderRepository.findByCreatedBy(customerName);
            }
            return orderRepository.findAll();
        }
        return List.of();
    }

    private boolean checkAuthoritiesRole(Collection<? extends GrantedAuthority> authorities, Roles role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(ROLE_PREFIX + role));
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
