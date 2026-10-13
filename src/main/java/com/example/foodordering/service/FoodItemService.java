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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<FoodItemResponse> getFoodItems(
            String categoryId, String search, Boolean available, Pageable pageable) {

        boolean hasCategory = StringUtils.hasText(categoryId);
        boolean hasSearch = StringUtils.hasText(search);

        Page<FoodItem> pageResult;

        if (hasCategory && hasSearch) {
            String trimmedSearch = search.trim();
            if (available != null) {
                pageResult = foodItemRepository.findByCategoryIdAndNameContainingIgnoreCaseAndAvailable(
                        categoryId.trim(), trimmedSearch, available, pageable);
            } else {
                pageResult = foodItemRepository.findByCategoryIdAndNameContainingIgnoreCase(
                        categoryId.trim(), trimmedSearch, pageable);
            }
        } else if (hasCategory) {
            if (available != null) {
                pageResult = foodItemRepository.findByCategoryIdAndAvailable(
                        categoryId.trim(), available, pageable);
            } else {
                pageResult = foodItemRepository.findByCategoryId(categoryId.trim(), pageable);
            }
        } else if (hasSearch) {
            String trimmedSearch = search.trim();
            if (available != null) {
                pageResult = foodItemRepository.findByNameContainingIgnoreCaseAndAvailable(
                        trimmedSearch, available, pageable);
            } else {
                pageResult = foodItemRepository.findByNameContainingIgnoreCase(trimmedSearch, pageable);
            }
        } else {
            if (available != null) {
                pageResult = foodItemRepository.findByAvailable(available, pageable);
            } else {
                pageResult = foodItemRepository.findAll(pageable);
            }
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (c1, c2) -> c1));

        Page<FoodItemResponse> responsePage = pageResult.map(item ->
                mapToFoodItemResponse(item, categoryNames.getOrDefault(item.getCategoryId(), "General")));

        return PageResponse.of(responsePage);
    }

    public FoodItemResponse getFoodItemById(String id) {
        FoodItem item = findFoodItemById(id);
        String categoryName = categoryRepository.findById(item.getCategoryId())
                .map(Category::getName)
                .orElse("General");
        return mapToFoodItemResponse(item, categoryName);
    }

    public FoodItemResponse createFoodItem(FoodItemRequest request) {
        validateCategory(request.getCategoryId());
        validatePrice(request.getPrice());

        FoodItem foodItem = FoodItem.builder()
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .categoryId(request.getCategoryId().trim())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null)
                .available(request.isAvailable())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        FoodItem saved = foodItemRepository.save(foodItem);
        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(Category::getName)
                .orElse("General");
        return mapToFoodItemResponse(saved, categoryName);
    }

    public FoodItemResponse updateFoodItem(String id, FoodItemRequest request) {
        FoodItem item = findFoodItemById(id);
        validateCategory(request.getCategoryId());
        validatePrice(request.getPrice());

        item.setName(request.getName().trim());
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription().trim());
        }
        item.setCategoryId(request.getCategoryId().trim());
        item.setPrice(request.getPrice());
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl().trim());
        }
        item.setAvailable(request.isAvailable());
        item.setUpdatedAt(Instant.now());

        FoodItem saved = foodItemRepository.save(item);
        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(Category::getName)
                .orElse("General");
        return mapToFoodItemResponse(saved, categoryName);
    }

    public void deleteFoodItem(String id) {
        FoodItem item = findFoodItemById(id);
        foodItemRepository.delete(item);
    }

    public FoodItem findFoodItemById(String id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + id));
    }

    private void validateCategory(String categoryId) {
        if (!categoryRepository.existsById(categoryId.trim())) {
            throw new BadRequestException("Category not found with id: " + categoryId);
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Price must be greater than zero");
        }
    }

    private FoodItemResponse mapToFoodItemResponse(FoodItem item, String categoryName) {
        return FoodItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .categoryId(item.getCategoryId())
                .categoryName(categoryName)
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .available(item.isAvailable())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
