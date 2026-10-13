package com.example.foodordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String foodItemId;
    private String foodName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;
}
