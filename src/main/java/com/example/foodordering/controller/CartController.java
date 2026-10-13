package com.example.foodordering.controller;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.ApiResponse;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.dto.UpdateCartItemRequest;
import com.example.foodordering.service.CartService;
import com.example.foodordering.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        String customerId = SecurityUtils.getCurrentUserId();
        CartResponse cart = cartService.getCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved successfully"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String customerId = SecurityUtils.getCurrentUserId();
        CartResponse cart = cartService.addToCart(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart successfully"));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        String customerId = SecurityUtils.getCurrentUserId();
        CartResponse cart = cartService.updateCartItem(customerId, itemId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart item updated successfully"));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(@PathVariable String itemId) {
        String customerId = SecurityUtils.getCurrentUserId();
        CartResponse cart = cartService.removeFromCart(customerId, itemId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart successfully"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        String customerId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared successfully"));
    }
}
