package com.example.foodordering.service;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderItemResponse;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.dto.PageResponse;
import com.example.foodordering.exception.BadRequestException;
import com.example.foodordering.exception.EmptyCartException;
import com.example.foodordering.exception.ForbiddenException;
import com.example.foodordering.exception.InvalidOrderStatusTransitionException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.CartItem;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.model.Order;
import com.example.foodordering.model.OrderItem;
import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.PaymentMethod;
import com.example.foodordering.model.PaymentStatus;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final FoodItemRepository foodItemRepository;
    private final UserService userService;

    public OrderResponse checkout(String customerId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(customerId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cart is empty. Please add food items before checking out.");
        }

        User customer = userService.findUserById(customerId);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            FoodItem food = foodItemRepository.findById(cartItem.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found with id: " + cartItem.getFoodItemId()));

            if (!food.isAvailable()) {
                throw new BadRequestException("Food item '" + food.getName() + "' is currently unavailable. Please remove it to proceed.");
            }

            BigDecimal unitPrice = food.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .foodItemId(food.getId())
                    .foodName(food.getName())
                    .unitPrice(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build());
        }

        PaymentMethod paymentMethod = request.getPaymentMethod() != null ?
                request.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY;

        Order order = Order.builder()
                .customerId(customerId)
                .customerName(customer.getName())
                .items(orderItems)
                .totalAmount(totalAmount)
                .deliveryAddress(request.getDeliveryAddress().trim())
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.PENDING)
                .orderStatus(OrderStatus.PLACED)
                .orderDate(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        // Clear cart only after successful order placement
        cartService.clearCart(customerId);

        return mapToOrderResponse(savedOrder);
    }

    public List<OrderResponse> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public PageResponse<OrderResponse> getCustomerOrders(String customerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId, pageable);
        return PageResponse.of(orderPage.map(this::mapToOrderResponse));
    }

    public OrderResponse getOrderById(String orderId, String customerId, boolean isAdmin) {
        Order order = findOrderById(orderId);

        if (!isAdmin && !order.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("You do not have permission to view this order.");
        }

        return mapToOrderResponse(order);
    }

    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAllByOrderByOrderDateDesc(pageable);
        return PageResponse.of(orderPage.map(this::mapToOrderResponse));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == newStatus) {
            return mapToOrderResponse(order);
        }

        validateStatusTransition(currentStatus, newStatus);

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(Instant.now());

        // Cash On Delivery is paid upon successful delivery
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    public Order findOrderById(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PLACED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.CANCELLED;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED || next == OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false; // Terminal states
        };

        if (!valid) {
            throw new InvalidOrderStatusTransitionException(
                    "Invalid order status transition from " + current + " to " + next);
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .foodItemId(item.getFoodItemId())
                        .foodName(item.getFoodName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .customerName(order.getCustomerName())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
