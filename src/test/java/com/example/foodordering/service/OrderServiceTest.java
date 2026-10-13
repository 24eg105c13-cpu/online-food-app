package com.example.foodordering.service;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.exception.EmptyCartException;
import com.example.foodordering.exception.ForbiddenException;
import com.example.foodordering.exception.InvalidOrderStatusTransitionException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.CartItem;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.model.Order;
import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.PaymentMethod;
import com.example.foodordering.model.PaymentStatus;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private FoodItem food1;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id("cust-1")
                .name("Alice Smith")
                .email("alice@example.com")
                .build();

        food1 = FoodItem.builder()
                .id("food-1")
                .name("Truffle Swiss Burger")
                .price(new BigDecimal("12.50"))
                .available(true)
                .build();

        cart = Cart.builder()
                .id("cart-1")
                .customerId("cust-1")
                .items(new ArrayList<>(List.of(new CartItem("food-1", 2))))
                .build();
    }

    @Test
    @DisplayName("Checkout: Successfully creates order, snapshots price, calculates total, and clears cart")
    void testCheckoutSuccess() {
        when(cartService.getOrCreateCart("cust-1")).thenReturn(cart);
        when(userService.findUserById("cust-1")).thenReturn(customer);
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(food1));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId("order-101");
            return o;
        });

        CheckoutRequest request = CheckoutRequest.builder()
                .deliveryAddress("456 Elm Avenue, Suite 4B")
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .build();

        OrderResponse response = orderService.checkout("cust-1", request);

        assertNotNull(response);
        assertEquals("order-101", response.getId());
        assertEquals("cust-1", response.getCustomerId());
        assertEquals(new BigDecimal("25.00"), response.getTotalAmount());
        assertEquals(OrderStatus.PLACED, response.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, response.getPaymentMethod());
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("12.50"), response.getItems().get(0).getUnitPrice());
        assertEquals(new BigDecimal("25.00"), response.getItems().get(0).getLineTotal());

        // Verify cart cleared
        verify(cartService).clearCart("cust-1");
    }

    @Test
    @DisplayName("Checkout: Rejects empty cart checkout")
    void testCheckoutEmptyCart() {
        Cart emptyCart = Cart.builder().customerId("cust-1").items(new ArrayList<>()).build();
        when(cartService.getOrCreateCart("cust-1")).thenReturn(emptyCart);

        CheckoutRequest request = CheckoutRequest.builder()
                .deliveryAddress("456 Elm Ave")
                .build();

        assertThrows(EmptyCartException.class, () -> orderService.checkout("cust-1", request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkout: Rejects checkout when an item has become unavailable")
    void testCheckoutUnavailableItem() {
        food1.setAvailable(false);
        when(cartService.getOrCreateCart("cust-1")).thenReturn(cart);
        when(userService.findUserById("cust-1")).thenReturn(customer);
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(food1));

        CheckoutRequest request = CheckoutRequest.builder()
                .deliveryAddress("456 Elm Ave")
                .build();

        assertThrows(BadRequestException.class, () -> orderService.checkout("cust-1", request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Customer Isolation: Prevents customer from viewing another customer's order")
    void testCustomerCannotViewOthersOrder() {
        Order order = Order.builder()
                .id("order-202")
                .customerId("different-customer-id")
                .build();

        when(orderRepository.findById("order-202")).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class, () ->
                orderService.getOrderById("order-202", "cust-1", false));
    }

    @Test
    @DisplayName("Admin Access: Allows admin to view any customer's order")
    void testAdminCanViewAnyOrder() {
        Order order = Order.builder()
                .id("order-202")
                .customerId("different-customer-id")
                .items(new ArrayList<>())
                .totalAmount(new BigDecimal("50.00"))
                .orderStatus(OrderStatus.PLACED)
                .build();

        when(orderRepository.findById("order-202")).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById("order-202", "admin-id", true);
        assertNotNull(response);
        assertEquals("order-202", response.getId());
    }

    @Test
    @DisplayName("Status Transition: Valid transitions PLACED -> CONFIRMED -> PREPARING -> OUT_FOR_DELIVERY -> DELIVERED")
    void testValidStatusTransitions() {
        Order order = Order.builder()
                .id("order-301")
                .customerId("cust-1")
                .orderStatus(OrderStatus.PLACED)
                .paymentMethod(PaymentMethod.CASH_ON_DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById("order-301")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // PLACED -> CONFIRMED
        OrderResponse r1 = orderService.updateOrderStatus("order-301", OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, r1.getOrderStatus());

        // CONFIRMED -> PREPARING
        OrderResponse r2 = orderService.updateOrderStatus("order-301", OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, r2.getOrderStatus());

        // PREPARING -> OUT_FOR_DELIVERY
        OrderResponse r3 = orderService.updateOrderStatus("order-301", OrderStatus.OUT_FOR_DELIVERY);
        assertEquals(OrderStatus.OUT_FOR_DELIVERY, r3.getOrderStatus());

        // OUT_FOR_DELIVERY -> DELIVERED (and Cash on delivery transitions to PAID!)
        OrderResponse r4 = orderService.updateOrderStatus("order-301", OrderStatus.DELIVERED);
        assertEquals(OrderStatus.DELIVERED, r4.getOrderStatus());
        assertEquals(PaymentStatus.PAID, r4.getPaymentStatus());
    }

    @Test
    @DisplayName("Status Transition: Invalid jump PLACED -> DELIVERED throws InvalidOrderStatusTransitionException")
    void testInvalidStatusTransition() {
        Order order = Order.builder()
                .id("order-401")
                .customerId("cust-1")
                .orderStatus(OrderStatus.PLACED)
                .build();

        when(orderRepository.findById("order-401")).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStatusTransitionException.class, () ->
                orderService.updateOrderStatus("order-401", OrderStatus.DELIVERED));
    }
}
