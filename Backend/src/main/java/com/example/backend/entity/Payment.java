package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId; // Payment ID (auto-generated)

    @Column(nullable = false)
    private String listingName;

    @Column(nullable = false)
    private Integer quantity;

    // Keep as String for consistency with existing price representation
    @Column( nullable = false)
    private String price;
}
