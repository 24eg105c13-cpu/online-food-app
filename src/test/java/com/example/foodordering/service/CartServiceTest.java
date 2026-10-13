package com.example.foodordering.service;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.CartItem;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CartRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private CartService cartService;

    private FoodItem foodItem1;
    private FoodItem unavailableFood;
    private Cart cart;

    @BeforeEach
    void setUp() {
        foodItem1 = FoodItem.builder()
                .id("food-1")
                .name("Angus Burger")
                .price(new BigDecimal("10.00"))
                .available(true)
                .build();

        unavailableFood = FoodItem.builder()
                .id("food-2")
                .name("Sold Out Pizza")
                .price(new BigDecimal("15.00"))
                .available(false)
                .build();

        cart = Cart.builder()
                .id("cart-1")
                .customerId("cust-1")
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Cart: Add item successfully calculates quantity and server-side total")
    void testAddToCartSuccess() {
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(foodItem1));
        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        AddToCartRequest request = AddToCartRequest.builder()
                .foodItemId("food-1")
                .quantity(2)
                .build();

        CartResponse response = cartService.addToCart("cust-1", request);

        assertNotNull(response);
        assertEquals(2, response.getTotalQuantity());
        assertEquals(new BigDecimal("20.00"), response.getTotalAmount());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("20.00"), response.getItems().get(0).getLineTotal());
    }

    @Test
    @DisplayName("Cart: Rejects adding unavailable food item")
    void testAddToCartUnavailable() {
        when(foodItemRepository.findById("food-2")).thenReturn(Optional.of(unavailableFood));

        AddToCartRequest request = AddToCartRequest.builder()
                .foodItemId("food-2")
                .quantity(1)
                .build();

        assertThrows(BadRequestException.class, () -> cartService.addToCart("cust-1", request));
    }

    @Test
    @DisplayName("Cart: Update quantity to 0 removes the item from cart")
    void testUpdateQuantityToZeroRemovesItem() {
        cart.getItems().add(new CartItem("food-1", 3));
        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.updateCartItem("cust-1", "food-1", 0);

        assertNotNull(response);
        assertEquals(0, response.getTotalQuantity());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
        assertTrue(response.getItems().isEmpty());
    }

    @Test
    @DisplayName("Cart: Remove item successfully removes item from cart")
    void testRemoveFromCart() {
        cart.getItems().add(new CartItem("food-1", 2));
        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.removeFromCart("cust-1", "food-1");

        assertNotNull(response);
        assertTrue(cart.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
    }

    @Test
    @DisplayName("Cart: Clear cart empties all items")
    void testClearCart() {
        cart.getItems().add(new CartItem("food-1", 2));
        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(cart));

        cartService.clearCart("cust-1");

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }
}
