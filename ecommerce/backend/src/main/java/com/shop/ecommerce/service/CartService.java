package com.shop.ecommerce.service;

import com.shop.ecommerce.dto.Dtos.*;
import com.shop.ecommerce.entity.Cart;
import com.shop.ecommerce.entity.CartItem;
import com.shop.ecommerce.entity.Product;
import com.shop.ecommerce.entity.User;
import com.shop.ecommerce.exception.BadRequestException;
import com.shop.ecommerce.exception.ResourceNotFoundException;
import com.shop.ecommerce.repository.CartRepository;
import com.shop.ecommerce.repository.ProductRepository;
import com.shop.ecommerce.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserContext userContext;

    @Transactional
    public CartResponse getCart() {
        return toResponse(getOrCreateCart());
    }

    @Transactional
    public CartResponse addItem(CartItemRequest req) {
        Cart cart = getOrCreateCart();
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.productId()));

        CartItem item = findItem(cart, product.getId());
        int newQty = (item == null ? 0 : item.getQuantity()) + req.quantity();
        if (newQty > product.getStock()) {
            throw new BadRequestException("Only " + product.getStock() + " units of '" + product.getName() + "' in stock");
        }
        if (item == null) {
            cart.getItems().add(CartItem.builder().cart(cart).product(product).quantity(newQty).build());
        } else {
            item.setQuantity(newQty);
        }
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(Long productId, int quantity) {
        Cart cart = getOrCreateCart();
        CartItem item = findItem(cart, productId);
        if (item == null) throw new ResourceNotFoundException("Item not in cart: " + productId);
        if (quantity > item.getProduct().getStock()) {
            throw new BadRequestException("Only " + item.getProduct().getStock() + " units in stock");
        }
        item.setQuantity(quantity);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long productId) {
        Cart cart = getOrCreateCart();
        CartItem item = findItem(cart, productId);
        if (item == null) throw new ResourceNotFoundException("Item not in cart: " + productId);
        cart.getItems().remove(item);   // orphanRemoval deletes the row
        return toResponse(cart);
    }

    @Transactional
    public void clear() {
        getOrCreateCart().getItems().clear();
    }

    private Cart getOrCreateCart() {
        User user = userContext.currentUser();
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartItem findItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(i -> {
            Product p = i.getProduct();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
            return new CartItemResponse(p.getId(), p.getName(), p.getImageUrl(), p.getPrice(), i.getQuantity(), subtotal);
        }).toList();
        BigDecimal total = items.stream().map(CartItemResponse::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, total);
    }
}
