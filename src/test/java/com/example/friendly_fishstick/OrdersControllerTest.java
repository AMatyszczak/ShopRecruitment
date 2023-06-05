package com.example.friendly_fishstick;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "CUSTOMER"})
    void customerCreateOrder(String role) throws Exception {
        var username = "username";
        var dto = new OrderDTO("name_1");
        var order = new Order("1", "name_1", username);

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf().asHeader())
                        .with(user(username).roles(role))
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).insert(captor.capture());
        var actualOrder = captor.getValue();

        assertNotNull(order.id());
        assertEquals(order.createdBy(), actualOrder.createdBy());
        assertEquals(order.name(), actualOrder.name());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void adminListsAllOrders() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_2", "customer_2"));
        doReturn(orders).when(orderRepository).findAll();
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void adminListsOrdersOfCustomer() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_1", "customer_2"));
        doReturn(orders).when(orderRepository).findByCreatedBy("name_1");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/orders").param("customerName", "name_1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    @WithMockUser(username = "customer_1", roles="CUSTOMER")
    void customerListsHisOrders() throws Exception {
        var username = "customer_1";

        var orders = List.of(new Order("1", "name_1", username),
                new Order("2", "name_2", username));
        doReturn(orders).when(orderRepository).findByCreatedBy(username);
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/orders").param("customerName", username))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    @WithMockUser(roles="CUSTOMER")
    void customerListsOderUserOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders").param("customerName", "customer_1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="CUSTOMER")
    void customerListsAllOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerDeletesOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/1").with(csrf().asHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="ADMIN")
    void adminDeleteOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/orders/1").with(csrf().asHeader()))
                .andExpect(status().is2xxSuccessful());

        verify(orderRepository).deleteById("1");
    }
}