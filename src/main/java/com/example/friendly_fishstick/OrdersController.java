package com.example.friendly_fishstick;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.friendly_fishstick.SecurityConfig.ROLE_PREFIX;

@RestController
@RequestMapping("/api/v1/orders")
@SecurityRequirement(name = "Shop API")
public class OrdersController {

    private final OrderRepository orderRepository;

    public OrdersController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Operation(summary = "Create new Order", security = @SecurityRequirement(name = "basicAuth"))
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public Map<String, String> createOrder(@RequestBody OrderDTO orderDto, Authentication auth) {
        var id = UUID.randomUUID().toString();
        orderRepository.insert(new Order(id, orderDto.name(), auth.getName()));
        return Map.of("id:", id);
    }

    @Operation(summary = "List orders", security = @SecurityRequirement(name = "basicAuth"))
    @GetMapping
    public List<Order> listOrders(@RequestParam(required = false) String customerName, Authentication auth) {
        if (checkAuthoritiesRole(auth.getAuthorities(), Role.CUSTOMER)) {
            if (Objects.isNull(customerName) || !customerName.equals(auth.getName())) {
                throw new UnauthorizedOperationException();
            }
            return orderRepository.findByCreatedBy(customerName);
        }
        if (checkAuthoritiesRole(auth.getAuthorities(), Role.ADMIN)) {
            if (Objects.nonNull(customerName)) {
                return orderRepository.findByCreatedBy(customerName);
            }
            return orderRepository.findAll();
        }
        return List.of();
    }

    private boolean checkAuthoritiesRole(Collection<? extends GrantedAuthority> authorities, Role role) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(ROLE_PREFIX + role));
    }

    @Operation(summary = "Remove Order", security = @SecurityRequirement(name = "basicAuth"))
    @DeleteMapping("{id}")
    public void removeOrder(@PathVariable("id") String orderId) {
        this.orderRepository.deleteById(orderId);
    }

    @SuppressWarnings({"unused", "EmptyMethod"})
    @ExceptionHandler(UnauthorizedOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    void unauthorizedOperationException(UnauthorizedOperationException ex) { }
}
