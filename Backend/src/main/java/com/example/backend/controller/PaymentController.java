package com.example.backend.controller;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.PaymentRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @GetMapping("/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllPayments(@RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            String email = jwtUtil.extractUsername(token.substring(7));
            User requester = userRepository.findByEmail(email).orElse(null);
            if (requester == null || requester.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(java.util.Map.of("message", "Access denied"));
            }

            return ResponseEntity.ok(paymentRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching payments: ", e);
            return ResponseEntity.status(500).body(java.util.Map.of("message", "Failed to fetch payments"));
        }
    }
}
