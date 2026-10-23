package com.shop.ecommerce.service;

import com.shop.ecommerce.dto.Dtos.*;
import com.shop.ecommerce.entity.*;
import com.shop.ecommerce.exception.BadRequestException;
import com.shop.ecommerce.exception.ResourceNotFoundException;
import com.shop.ecommerce.repository.CartRepository;
import com.shop.ecommerce.repository.OrderRepository;
import com.shop.ecommerce.repository.ProductRepository;
import com.shop.ecommerce.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserContext userContext;

    /**
     * Converts the current user's cart into an order. Everything runs in ONE transaction:
     * if any item is out of stock, stock changes and order creation are rolled back together.
     */
    @Transactional
    public OrderResponse placeOrder(String shippingAddress) {
        User user = userContext.currentUser();
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(shippingAddress.trim())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = productRepository.findByIdForUpdate(ci.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product no longer exists"));
            if (p.getStock() < ci.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + p.getName()
                        + "' (available: " + p.getStock() + ")");
            }
            p.setStock(p.getStock() - ci.getQuantity());
            order.getItems().add(OrderItem.builder()
                    .order(order).product(p).productName(p.getName())
                    .quantity(ci.getQuantity()).price(p.getPrice()).build());
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        order.setTotalAmount(total);
        cart.getItems().clear();

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(int page, int size) {
        User user = userContext.currentUser();
        return PageResponse.from(orderRepository.findByUserId(user.getId(), pageable(page, size)).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> allOrders(int page, int size) {
        return PageResponse.from(orderRepository.findAll(pageable(page, size)).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = find(id);
        assertOwnerOrAdmin(order);
        return toResponse(order);
    }

    /** Customers can cancel their own PENDING orders; stock is returned. */
    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = find(id);
        assertOwnerOrAdmin(order);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }
        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        Order order = find(id);
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order is already " + order.getStatus());
        }
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }
        order.setStatus(newStatus);
        return toResponse(order);
    }

    private void restoreStock(Order order) {
        for (OrderItem oi : order.getItems()) {
            productRepository.findByIdForUpdate(oi.getProduct().getId())
                    .ifPresent(p -> p.setStock(p.getStock() + oi.getQuantity()));
        }
    }

    private Order find(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private void assertOwnerOrAdmin(Order order) {
        User current = userContext.currentUser();
        if (!userContext.isAdmin(current) && !order.getUser().getId().equals(current.getId())) {
            throw new AccessDeniedException("Not your order");
        }
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getItems().stream().map(i -> new OrderItemResponse(
                i.getProduct().getId(), i.getProductName(), i.getQuantity(), i.getPrice(),
                i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();
        return new OrderResponse(o.getId(), o.getUser().getEmail(), o.getTotalAmount(), o.getStatus().name(),
                o.getShippingAddress(), o.getCreatedAt(), items);
    }
}
