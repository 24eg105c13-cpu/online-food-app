package com.example.foodordering.config;

import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminPassword)) {
            log.info("ADMIN_EMAIL and ADMIN_PASSWORD are not configured. To initialize an admin account, configure these environment variables.");
            return;
        }

        try {
            String normalizedEmail = adminEmail.trim().toLowerCase();
            if (userRepository.existsByEmail(normalizedEmail)) {
                log.info("Admin account already exists for email: {}", normalizedEmail);
                return;
            }

            User adminUser = User.builder()
                    .name("System Administrator")
                    .email(normalizedEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .phone("1800-FOOD-ADMIN")
                    .address("HQ Operations")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            userRepository.save(adminUser);
            log.info("Initial administrator account created successfully for: {}", normalizedEmail);
        } catch (Exception e) {
            log.warn("Could not check/initialize admin account (MongoDB may be offline): {}", e.getMessage());
        }
    }
}
