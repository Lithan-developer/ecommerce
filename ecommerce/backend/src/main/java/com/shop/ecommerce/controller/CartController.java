package com.shop.ecommerce.controller;

import com.shop.ecommerce.dto.Dtos.*;
import com.shop.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse get() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponse add(@Valid @RequestBody CartItemRequest req) {
        return cartService.addItem(req);
    }

    @PutMapping("/items/{productId}")
    public CartResponse update(@PathVariable Long productId, @Valid @RequestBody UpdateQuantityRequest req) {
        return cartService.updateQuantity(productId, req.quantity());
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable Long productId) {
        return cartService.removeItem(productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear() {
        cartService.clear();
    }
}
