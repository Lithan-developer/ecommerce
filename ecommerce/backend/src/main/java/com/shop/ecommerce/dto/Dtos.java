package com.shop.ecommerce.dto;

import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** All request/response DTOs grouped in one holder class. */
public final class Dtos {
    private Dtos() {}

    // ---------- Auth ----------
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record AuthResponse(String token, Long id, String name, String email, String role) {}

    // ---------- Catalog ----------
    public record CategoryRequest(@NotBlank @Size(max = 100) String name) {}

    public record CategoryResponse(Long id, String name) {}

    public record ProductRequest(
            @NotBlank @Size(max = 255) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin(value = "0.01") BigDecimal price,
            @NotNull @Min(0) Integer stock,
            String imageUrl,
            @NotNull Long categoryId) {}

    public record ProductResponse(Long id, String name, String description, BigDecimal price,
                                  Integer stock, String imageUrl, Long categoryId, String categoryName) {}

    // ---------- Cart ----------
    public record CartItemRequest(@NotNull Long productId, @NotNull @Min(1) Integer quantity) {}

    public record UpdateQuantityRequest(@NotNull @Min(1) Integer quantity) {}

    public record CartItemResponse(Long productId, String name, String imageUrl, BigDecimal price,
                                   Integer quantity, BigDecimal subtotal) {}

    public record CartResponse(List<CartItemResponse> items, BigDecimal total) {}

    // ---------- Orders ----------
    public record PlaceOrderRequest(@NotBlank @Size(max = 500) String shippingAddress) {}

    public record OrderItemResponse(Long productId, String productName, Integer quantity,
                                    BigDecimal price, BigDecimal subtotal) {}

    public record OrderResponse(Long id, String customerEmail, BigDecimal totalAmount, String status,
                                String shippingAddress, LocalDateTime createdAt, List<OrderItemResponse> items) {}

    // ---------- Common ----------
    public record PageResponse<T>(List<T> content, int page, int size, long totalElements,
                                  int totalPages, boolean last) {
        public static <T> PageResponse<T> from(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                    p.getTotalElements(), p.getTotalPages(), p.isLast());
        }
    }

    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                                String path, Map<String, String> fieldErrors) {}
}
