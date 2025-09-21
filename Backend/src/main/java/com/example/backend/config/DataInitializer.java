package com.example.backend.config;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        // Check if admin user already exists by email or if any admin exists
        if (!userRepository.existsByEmail("Admin@gmail.com") && !userRepository.existsByRole(Role.ADMIN)) {
            User admin = User.builder()
                    .username("Admin")
                    .email("Admin@gmail.com")
                    .password(passwordEncoder.encode("Admin@12"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created successfully with email: Admin@gmail.com");
        } else {
            log.info("Admin user already exists, skipping creation");
        }
    }
}
