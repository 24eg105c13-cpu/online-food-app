package com.example.foodordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemResponse {
    private String id;
    private String name;
    private String description;
    private String categoryId;
    private String categoryName;
    private BigDecimal price;
    private String imageUrl;
    private boolean available;
    private Instant createdAt;
    private Instant updatedAt;
}
