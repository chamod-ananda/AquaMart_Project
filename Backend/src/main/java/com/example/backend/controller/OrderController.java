package com.example.backend.controller;

import com.example.backend.dto.OrderDTO;
import com.example.backend.service.OrderService;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final JWTUtil jwtUtil;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new SecurityException("Invalid or expired token");
        }
        return userId;
    }

    @PostMapping("/pay/{listingId}")
    public ResponseEntity<OrderDTO> payAndCreateOrder(@PathVariable Long listingId,
                                                      @RequestParam(name = "quantity", defaultValue = "1") Integer qty,
                                                      @RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(orderService.createPaidOrder(userId, listingId, qty));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderDTO>> myOrders(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }
}
