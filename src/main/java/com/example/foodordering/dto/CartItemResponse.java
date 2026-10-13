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
public class CartItemResponse {
    private String foodItemId;
    private String foodName;
    private BigDecimal unitPrice;
    private String imageUrl;
    private int quantity;
    private BigDecimal lineTotal;
    private boolean available;
}
