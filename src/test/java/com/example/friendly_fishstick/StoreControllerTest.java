package com.example.friendly_fishstick;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.mongodb.MongoWriteException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void userCreateOrder() throws Exception {
        var order = new Order("1", "name_1", "customer_1");

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(order);

        mockMvc.perform(post("/api/v1/store")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        verify(orderRepository).insert(order);
    }

    @Test
    void adminCreateOrder() throws Exception {
        var order = new Order("1", "name_1", "customer_1");

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(order);

        mockMvc.perform(post("/api/v1/store")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        verify(orderRepository).insert(order);
    }

    @Test
    void createDuplicatedOrder() throws Exception {
        var order = new OrderDTO("name_1", "customer_1");

        var exception = Mockito.mock(MongoWriteException.class);
        doThrow(exception).when(orderRepository).insert(any(Order.class));

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(order);

        mockMvc.perform(post("/api/v1/store")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("Order with provided Id already exists"));

        verify(orderRepository).insert(any(Order.class));
    }

    @Test
    void adminListsOrders() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_2", "customer_2"));
        doReturn(orders).when(orderRepository).findAll();
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/store"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    void adminListsOrdersOfCustomer() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_1", "customer_2"));
        doReturn(orders).when(orderRepository).findByCreatedBy("name_1");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/store").param("createdBy", "name_1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    void customerListsHisOrders() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_2", "customer_1"));
        doReturn(orders).when(orderRepository).findByCreatedBy("name_1");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/store").param("createdBy", "customer_1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    void customerListsOderUserOrders() throws Exception {
        mockMvc.perform(get("/api/v1/store").param("createdBy", "customer_1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerListsAllOrders() throws Exception {
        mockMvc.perform(get("/api/v1/store"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customerDeletesOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/store"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/store/1"))
                .andExpect(status().is2xxSuccessful());

        verify(orderRepository).deleteById("1");
    }
}