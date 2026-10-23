package com.shop.ecommerce.config;

import com.shop.ecommerce.entity.Category;
import com.shop.ecommerce.entity.Product;
import com.shop.ecommerce.entity.Role;
import com.shop.ecommerce.entity.User;
import com.shop.ecommerce.repository.CategoryRepository;
import com.shop.ecommerce.repository.ProductRepository;
import com.shop.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Seeds a default admin account and some sample catalog data on first run. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shop.com")) {
            userRepository.save(User.builder().name("Admin").email("admin@shop.com")
                    .password(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).build());
        }
        if (categoryRepository.count() == 0) {
            Category electronics = categoryRepository.save(Category.builder().name("Electronics").build());
            Category books = categoryRepository.save(Category.builder().name("Books").build());
            Category clothing = categoryRepository.save(Category.builder().name("Clothing").build());

            productRepository.save(product("Wireless Mouse", "Ergonomic 2.4GHz wireless mouse", "799.00", 50, electronics));
            productRepository.save(product("Mechanical Keyboard", "Blue switch, backlit", "2499.00", 25, electronics));
            productRepository.save(product("Clean Code", "A Handbook of Agile Software Craftsmanship", "599.00", 40, books));
            productRepository.save(product("Effective Java", "Joshua Bloch, 3rd edition", "749.00", 30, books));
            productRepository.save(product("Cotton T-Shirt", "100% cotton, regular fit", "399.00", 100, clothing));
        }
    }

    private Product product(String name, String desc, String price, int stock, Category category) {
        return Product.builder().name(name).description(desc).price(new BigDecimal(price))
                .stock(stock).category(category).build();
    }
}
