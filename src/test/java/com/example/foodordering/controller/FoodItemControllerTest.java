package com.example.foodordering.controller;

import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.dto.PageResponse;
import com.example.foodordering.exception.GlobalExceptionHandler;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.service.FoodItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FoodItemControllerTest {

    @Mock
    private FoodItemService foodItemService;

    @InjectMocks
    private FoodItemController foodItemController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(foodItemController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /api/foods: Returns paginated list of foods")
    void testGetFoods() throws Exception {
        FoodItemResponse item = FoodItemResponse.builder()
                .id("f-1")
                .name("Wood-Fired Pizza")
                .price(new BigDecimal("14.00"))
                .available(true)
                .build();

        PageResponse<FoodItemResponse> pageResponse = PageResponse.<FoodItemResponse>builder()
                .content(Collections.singletonList(item))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(foodItemService.getFoodItems(any(), any(), any(), any(Pageable.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/foods")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Wood-Fired Pizza"));
    }

    @Test
    @DisplayName("GET /api/foods/{id}: Returns 404 when item does not exist")
    void testGetFoodByIdNotFound() throws Exception {
        when(foodItemService.getFoodItemById("missing-id"))
                .thenThrow(new ResourceNotFoundException("Food item not found with id: missing-id"));

        mockMvc.perform(get("/api/foods/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Food item not found with id: missing-id"));
    }

    @Test
    @DisplayName("POST /api/foods: Valid request returns 201 Created")
    void testCreateFoodSuccess() throws Exception {
        FoodItemRequest request = FoodItemRequest.builder()
                .name("Spicy Ramen")
                .categoryId("cat-2")
                .price(new BigDecimal("11.50"))
                .available(true)
                .build();

        FoodItemResponse response = FoodItemResponse.builder()
                .id("f-2")
                .name("Spicy Ramen")
                .categoryId("cat-2")
                .price(new BigDecimal("11.50"))
                .available(true)
                .build();

        when(foodItemService.createFoodItem(any(FoodItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("f-2"));
    }

    @Test
    @DisplayName("POST /api/foods: Negative price returns 400 Bad Request")
    void testCreateFoodNegativePrice() throws Exception {
        FoodItemRequest request = FoodItemRequest.builder()
                .name("Spicy Ramen")
                .categoryId("cat-2")
                .price(new BigDecimal("-5.00"))
                .available(true)
                .build();

        mockMvc.perform(post("/api/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }
}
