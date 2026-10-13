package com.example.foodordering.service;

import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.dto.PageResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Category;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FoodItemService foodItemService;

    private FoodItem sampleFood;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        sampleCategory = Category.builder()
                .id("cat-1")
                .name("Gourmet Burgers")
                .active(true)
                .build();

        sampleFood = FoodItem.builder()
                .id("food-1")
                .name("Truffle Swiss Burger")
                .description("Angus beef with black truffle")
                .categoryId("cat-1")
                .price(new BigDecimal("12.99"))
                .available(true)
                .build();
    }

    @Test
    @DisplayName("Create Food: Valid request succeeds and assigns category name")
    void testCreateFoodSuccess() {
        FoodItemRequest request = FoodItemRequest.builder()
                .name("Truffle Swiss Burger")
                .categoryId("cat-1")
                .price(new BigDecimal("12.99"))
                .description("Angus beef with black truffle")
                .available(true)
                .build();

        when(categoryRepository.existsById("cat-1")).thenReturn(true);
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(sampleFood);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));

        FoodItemResponse response = foodItemService.createFoodItem(request);

        assertNotNull(response);
        assertEquals("Truffle Swiss Burger", response.getName());
        assertEquals("Gourmet Burgers", response.getCategoryName());
        assertEquals(new BigDecimal("12.99"), response.getPrice());
    }

    @Test
    @DisplayName("Create Food: Throws BadRequestException when category does not exist")
    void testCreateFoodInvalidCategory() {
        FoodItemRequest request = FoodItemRequest.builder()
                .name("Truffle Swiss Burger")
                .categoryId("non-existent-cat")
                .price(new BigDecimal("12.99"))
                .build();

        when(categoryRepository.existsById("non-existent-cat")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> foodItemService.createFoodItem(request));
        verify(foodItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Food: Throws BadRequestException when price is zero or negative")
    void testCreateFoodInvalidPrice() {
        FoodItemRequest request = FoodItemRequest.builder()
                .name("Free Burger")
                .categoryId("cat-1")
                .price(new BigDecimal("0.00"))
                .build();

        when(categoryRepository.existsById("cat-1")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> foodItemService.createFoodItem(request));
    }

    @Test
    @DisplayName("Get Food: Returns food item by valid ID")
    void testGetFoodByIdSuccess() {
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(sampleFood));
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(sampleCategory));

        FoodItemResponse response = foodItemService.getFoodItemById("food-1");

        assertNotNull(response);
        assertEquals("food-1", response.getId());
        assertEquals("Truffle Swiss Burger", response.getName());
    }

    @Test
    @DisplayName("Get Food: Throws ResourceNotFoundException for missing ID")
    void testGetFoodByIdNotFound() {
        when(foodItemRepository.findById("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> foodItemService.getFoodItemById("missing-id"));
    }

    @Test
    @DisplayName("List Foods: Searches by query with pagination")
    void testGetFoodItemsWithSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FoodItem> page = new PageImpl<>(Collections.singletonList(sampleFood));

        when(foodItemRepository.findByNameContainingIgnoreCaseAndAvailable("truffle", true, pageable))
                .thenReturn(page);
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(sampleCategory));

        PageResponse<FoodItemResponse> result = foodItemService.getFoodItems(null, "truffle", true, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Truffle Swiss Burger", result.getContent().get(0).getName());
    }
}
