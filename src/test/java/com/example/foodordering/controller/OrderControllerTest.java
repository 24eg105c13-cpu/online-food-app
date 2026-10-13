package com.example.foodordering.controller;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.dto.OrderStatusUpdateRequest;
import com.example.foodordering.exception.EmptyCartException;
import com.example.foodordering.exception.GlobalExceptionHandler;
import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.PaymentMethod;
import com.example.foodordering.model.PaymentStatus;
import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.security.CustomUserDetails;
import com.example.foodordering.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Setup security context with mock customer
        User user = User.builder()
                .id("cust-1")
                .name("Alice Smith")
                .email("alice@example.com")
                .role(Role.CUSTOMER)
                .passwordHash("hash")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/orders: Successful checkout returns 201 Created")
    void testCheckoutSuccess() throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .deliveryAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id("order-999")
                .customerId("cust-1")
                .customerName("Alice Smith")
                .items(new ArrayList<>())
                .totalAmount(new BigDecimal("35.00"))
                .deliveryAddress("123 Maple Street")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PLACED)
                .build();

        when(orderService.checkout(eq("cust-1"), any(CheckoutRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("order-999"))
                .andExpect(jsonPath("$.data.totalAmount").value(35.00));
    }

    @Test
    @DisplayName("POST /api/orders: Empty cart checkout returns 400 Bad Request")
    void testCheckoutEmptyCart() throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .deliveryAddress("123 Maple Street")
                .build();

        when(orderService.checkout(eq("cust-1"), any(CheckoutRequest.class)))
                .thenThrow(new EmptyCartException("Cart is empty. Please add food items before checking out."));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Cart is empty. Please add food items before checking out."));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status: Status update returns updated order")
    void testUpdateStatusSuccess() throws Exception {
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.CONFIRMED)
                .build();

        OrderResponse updated = OrderResponse.builder()
                .id("order-999")
                .orderStatus(OrderStatus.CONFIRMED)
                .build();

        when(orderService.updateOrderStatus("order-999", OrderStatus.CONFIRMED)).thenReturn(updated);

        mockMvc.perform(put("/api/orders/order-999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderStatus").value("CONFIRMED"));
    }
}
