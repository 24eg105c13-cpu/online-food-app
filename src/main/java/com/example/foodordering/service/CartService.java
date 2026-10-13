package com.example.foodordering.service;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.CartItemResponse;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.CartItem;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CartRepository;
import com.example.foodordering.repository.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final FoodItemRepository foodItemRepository;

    public CartResponse getCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(String customerId, AddToCartRequest request) {
        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + request.getFoodItemId()));

        if (!foodItem.isAvailable()) {
            throw new BadRequestException("Food item '" + foodItem.getName() + "' is currently unavailable");
        }

        if (request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than zero");
        }

        Cart cart = getOrCreateCart(customerId);
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getFoodItemId().equals(request.getFoodItemId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            cart.getItems().add(new CartItem(request.getFoodItemId(), request.getQuantity()));
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse updateCartItem(String customerId, String foodItemId, int quantity) {
        Cart cart = getOrCreateCart(customerId);

        if (quantity <= 0) {
            return removeFromCart(customerId, foodItemId);
        }

        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + foodItemId));

        if (!foodItem.isAvailable()) {
            throw new BadRequestException("Food item '" + foodItem.getName() + "' is currently unavailable");
        }

        boolean found = false;
        for (CartItem item : cart.getItems()) {
            if (item.getFoodItemId().equals(foodItemId)) {
                item.setQuantity(quantity);
                found = true;
                break;
            }
        }

        if (!found) {
            cart.getItems().add(new CartItem(foodItemId, quantity));
        }

        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public CartResponse removeFromCart(String customerId, String foodItemId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().removeIf(i -> i.getFoodItemId().equals(foodItemId));
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    public void clearCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .customerId(customerId)
                        .items(new ArrayList<>())
                        .updatedAt(Instant.now())
                        .build()));
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (CartItem item : cart.getItems()) {
            Optional<FoodItem> foodOpt = foodItemRepository.findById(item.getFoodItemId());
            if (foodOpt.isPresent()) {
                FoodItem food = foodOpt.get();
                BigDecimal lineTotal = food.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                totalAmount = totalAmount.add(lineTotal);
                totalQuantity += item.getQuantity();

                itemResponses.add(CartItemResponse.builder()
                        .foodItemId(food.getId())
                        .foodName(food.getName())
                        .unitPrice(food.getPrice())
                        .imageUrl(food.getImageUrl())
                        .quantity(item.getQuantity())
                        .lineTotal(lineTotal)
                        .available(food.isAvailable())
                        .build());
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .customerId(cart.getCustomerId())
                .items(itemResponses)
                .totalQuantity(totalQuantity)
                .totalAmount(totalAmount)
                .build();
    }
}
