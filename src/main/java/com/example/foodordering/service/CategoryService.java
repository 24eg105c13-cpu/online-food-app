package com.example.foodordering.service;

import com.example.foodordering.dto.CategoryRequest;
import com.example.foodordering.dto.CategoryResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Category;
import com.example.foodordering.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActive(true).stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        Category category = findCategoryById(id);
        return mapToCategoryResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String trimmedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BadRequestException("Category with name '" + trimmedName + "' already exists");
        }

        Category category = Category.builder()
                .name(trimmedName)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null)
                .active(request.isActive())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = findCategoryById(id);
        String trimmedName = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(trimmedName) &&
                categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new BadRequestException("Category with name '" + trimmedName + "' already exists");
        }

        category.setName(trimmedName);
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().trim());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl().trim());
        }
        category.setActive(request.isActive());
        category.setUpdatedAt(Instant.now());

        Category saved = categoryRepository.save(category);
        return mapToCategoryResponse(saved);
    }

    public void deleteCategory(String id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    public Category findCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .active(category.isActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
