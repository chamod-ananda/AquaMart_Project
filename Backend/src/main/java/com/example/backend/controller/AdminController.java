package com.example.backend.controller;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repo.UserRepository;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    // Get all admin users
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAdmins(@RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            String email = jwtUtil.extractUsername(token.substring(7));
            Optional<User> adminUser = userRepository.findByEmail(email);

            if (adminUser.isEmpty() || adminUser.get().getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Access denied"));
            }

            List<User> admins = userRepository.findByRole(Role.ADMIN);

            // Return admin data without passwords
            List<Map<String, Object>> adminList = admins.stream()
                    .map(admin -> {
                        Map<String, Object> adminMap = new HashMap<>();
                        adminMap.put("id", admin.getId());
                        adminMap.put("username", admin.getUsername());
                        adminMap.put("email", admin.getEmail());
                        adminMap.put("role", admin.getRole().toString());
                        return adminMap;
                    })
                    .toList();

            return ResponseEntity.ok(adminList);
        } catch (Exception e) {
            log.error("Error fetching admins: ", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Failed to fetch admins"));
        }
    }

    // Add new admin
    @PostMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addNewAdmin(@RequestHeader("Authorization") String token,
                                         @RequestBody Map<String, String> adminData) {
        try {
            // Verify admin access
            String email = jwtUtil.extractUsername(token.substring(7));
            Optional<User> adminUser = userRepository.findByEmail(email);

            if (adminUser.isEmpty() || adminUser.get().getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Access denied"));
            }

            String username = adminData.get("username");
            String adminEmail = adminData.get("email");
            String password = adminData.get("password");

            // Validation
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Username is required"));
            }
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Email is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Password is required"));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Password must be at least 6 characters"));
            }

            // Check if email already exists
            if (userRepository.existsByEmail(adminEmail)) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Email already exists"));
            }

            // Check if username already exists
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Username already exists"));
            }

            // Create new admin
            User newAdmin = User.builder()
                    .username(username.trim())
                    .email(adminEmail.trim())
                    .password(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .build();

            User savedAdmin = userRepository.save(newAdmin);
            log.info("New admin created: {} by {}", savedAdmin.getEmail(), email);

            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", savedAdmin.getId());
            adminInfo.put("username", savedAdmin.getUsername());
            adminInfo.put("email", savedAdmin.getEmail());
            adminInfo.put("role", savedAdmin.getRole().toString());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin created successfully");
            response.put("admin", adminInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating admin: ", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Failed to create admin"));
        }
    }

    // Delete admin (but prevent deleting the last admin)
    @DeleteMapping("/admins/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAdmin(@RequestHeader("Authorization") String token,
                                         @PathVariable Long adminId) {
        try {
            // Verify admin access
            String email = jwtUtil.extractUsername(token.substring(7));
            Optional<User> adminUser = userRepository.findByEmail(email);

            if (adminUser.isEmpty() || adminUser.get().getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Access denied"));
            }

            // Check if admin exists
            Optional<User> targetAdmin = userRepository.findById(adminId);
            if (targetAdmin.isEmpty() || targetAdmin.get().getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Admin not found"));
            }

            // Prevent self-deletion
            if (adminUser.get().getId().equals(adminId)) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Cannot delete your own admin account"));
            }

            // Check if this is the last admin
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Cannot delete the last admin"));
            }

            userRepository.deleteById(adminId);
            log.info("Admin {} deleted by {}", targetAdmin.get().getEmail(), email);

            return ResponseEntity.ok(Collections.singletonMap("message", "Admin deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting admin: ", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Failed to delete admin"));
        }
    }

    // Get admin statistics
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminStats(@RequestHeader("Authorization") String token) {
        try {
            // Verify admin access
            String email = jwtUtil.extractUsername(token.substring(7));
            Optional<User> adminUser = userRepository.findByEmail(email);

            if (adminUser.isEmpty() || adminUser.get().getRole() != Role.ADMIN) {
                return ResponseEntity.status(403).body(Collections.singletonMap("message", "Access denied"));
            }

            long totalUsers = userRepository.count();
            long totalAdmins = userRepository.countByRole(Role.ADMIN);
            long totalRegularUsers = userRepository.countByRole(Role.USER);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", totalUsers);
            stats.put("totalAdmins", totalAdmins);
            stats.put("totalRegularUsers", totalRegularUsers);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching admin stats: ", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Failed to fetch statistics"));
        }
    }
}
