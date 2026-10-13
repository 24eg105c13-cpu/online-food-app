package com.example.foodordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String id;
    private String customerId;
    @Builder.Default
    private List<CartItemResponse> items = new ArrayList<>();
    private int totalQuantity;
    private BigDecimal totalAmount;
}
