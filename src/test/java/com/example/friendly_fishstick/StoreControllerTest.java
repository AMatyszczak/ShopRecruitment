package com.example.friendly_fishstick;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    @WithMockUser(username = "customer_1", roles="ADMIN")
    void customerCreateOrder() throws Exception {
        var dto = new OrderDTO("name_1");
        var order = new Order("1", "name_1", "customer_1");

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(dto);

        mockMvc.perform(post("/api/v1/store")
                        .with(csrf().asHeader())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).insert(captor.capture());
        var actualOrder = captor.getValue();

        assertNotNull(order.id());
        assertEquals(order.createdBy(), actualOrder.createdBy());
        assertEquals(order.name(), actualOrder.name());
    }

    @Test
    @WithMockUser(username = "customer_1", roles="ADMIN")
    void adminCreateOrder() throws Exception {
        var order = new Order("1", "name_1", "customer_1");

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(order);

        mockMvc.perform(post("/api/v1/store").with(csrf().asHeader())
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());

        var captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).insert(captor.capture());
        var actualOrder = captor.getValue();

        assertNotNull(order.id());
        assertEquals(order.createdBy(), actualOrder.createdBy());
        assertEquals(order.name(), actualOrder.name());
    }

    @Test
    @WithMockUser(roles="ADMIN")
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
    @WithMockUser(roles="ADMIN")
    void adminListsOrdersOfCustomer() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_1", "customer_2"));
        doReturn(orders).when(orderRepository).findByCreatedBy("name_1");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/store").param("customerName", "name_1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    @WithMockUser(username = "customer_1", roles="CUSTOMER")
    void customerListsHisOrders() throws Exception {
        var orders = List.of(new Order("1", "name_1", "customer_1"),
                new Order("2", "name_2", "customer_1"));
        doReturn(orders).when(orderRepository).findByCreatedBy("customer_1");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(orders);

        mockMvc.perform(get("/api/v1/store").param("customerName", "customer_1"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(json));
    }

    @Test
    @WithMockUser(roles="CUSTOMER")
    void customerListsOderUserOrders() throws Exception {
        mockMvc.perform(get("/api/v1/store").param("customerName", "customer_1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="CUSTOMER")
    void customerListsAllOrders() throws Exception {
        mockMvc.perform(get("/api/v1/store"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerDeletesOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/store/1").with(csrf().asHeader()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles="ADMIN")
    void adminDeleteOrder() throws Exception {
        mockMvc.perform(delete("/api/v1/store/1").with(csrf().asHeader()))
                .andExpect(status().is2xxSuccessful());

        verify(orderRepository).deleteById("1");
    }
}