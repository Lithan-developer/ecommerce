package com.shop.ecommerce.security;

import com.shop.ecommerce.entity.Role;
import com.shop.ecommerce.entity.User;
import com.shop.ecommerce.exception.ResourceNotFoundException;
import com.shop.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the currently authenticated user from the SecurityContext. */
@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserRepository userRepository;

    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }
}
