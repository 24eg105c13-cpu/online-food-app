package com.example.foodordering.controller;

import com.example.foodordering.dto.ApiResponse;
import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.dto.PageResponse;
import com.example.foodordering.service.FoodItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FoodItemResponse>>> getFoodItems(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = "desc".equalsIgnoreCase(direction) ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sort);

        PageResponse<FoodItemResponse> result = foodItemService.getFoodItems(categoryId, search, available, pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Food items retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemResponse>> getFoodItemById(@PathVariable String id) {
        FoodItemResponse response = foodItemService.getFoodItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Food item retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FoodItemResponse>> createFoodItem(@Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse created = foodItemService.createFoodItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Food item created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItem(
            @PathVariable String id,
            @Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse updated = foodItemService.updateFoodItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Food item updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFoodItem(@PathVariable String id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Food item deleted successfully"));
    }
}
