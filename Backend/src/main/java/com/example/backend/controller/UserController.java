package com.example.backend.controller;

import com.example.backend.dto.UserDTO;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    // Get logged-in user details
    @GetMapping("/settings")
    public ResponseEntity<UserDTO> getUserSettings(Authentication authentication) {
        String email = authentication.getName(); // comes from JWTAuthFilter
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO dto = new UserDTO(user.getUsername(), user.getEmail(), ""); // don't send password
        return ResponseEntity.ok(dto);
    }

    // Update logged-in user details
    @PutMapping("/settings")
    public ResponseEntity<?> updateUserSettings(
            Authentication authentication,
            @RequestBody UserDTO updateDTO
    ) {
        String currentEmail = authentication.getName();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if new email is taken by another user
        if (!updateDTO.getEmail().equalsIgnoreCase(currentEmail)
                && userRepository.existsByEmail(updateDTO.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Email is already in use by another account"));
        }

        user.setUsername(updateDTO.getUsername());
        user.setEmail(updateDTO.getEmail());

        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                    .encode(updateDTO.getPassword()));
        }

        userRepository.save(user);

        UserDTO dto = new UserDTO(user.getUsername(), user.getEmail(), "");
        return ResponseEntity.ok(dto);
    }

    // Get all users (for testing purposes)
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();

        // Return user data without passwords for security
        List<Object> usersList = users.stream()
                .map(user -> new Object() {
                    public final Long id = user.getId();
                    public final String username = user.getUsername();
                    public final String email = user.getEmail();
                    public final String role = user.getRole().name();
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(usersList);
    }

    // Get user count
    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        long count = userRepository.count();
        return ResponseEntity.ok(Map.of("totalUsers", count));
    }

}