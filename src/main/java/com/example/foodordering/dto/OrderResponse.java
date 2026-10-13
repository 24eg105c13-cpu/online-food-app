package com.example.foodordering.dto;

import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.PaymentMethod;
import com.example.foodordering.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String customerId;
    private String customerName;
    @Builder.Default
    private List<OrderItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private Instant orderDate;
    private Instant createdAt;
    private Instant updatedAt;
}
