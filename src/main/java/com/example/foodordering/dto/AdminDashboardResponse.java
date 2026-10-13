package com.example.foodordering.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private long totalCustomers;
    private long totalFoodItems;
    private long totalCategories;
    @Builder.Default
    private Map<String, Long> statusCounts = new HashMap<>();
}
