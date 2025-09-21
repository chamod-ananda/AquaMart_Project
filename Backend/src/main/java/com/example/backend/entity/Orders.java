package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "orders")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "listing_id")
    private Listings listing;

    @Column(nullable = false)
    private Integer quantity;

    // Keep as String to align with Listings.price type
    @Column(nullable = false)
    private String unitPrice;

    @Column(nullable = false)
    private String total;

    @Column(nullable = false)
    private String status; // e.g., PAID

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
