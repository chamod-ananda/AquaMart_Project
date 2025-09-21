package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "items")
public class Fish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status; // AVAILABLE, NOT_AVAILABLE

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String imageUrl;  // saved after uploading to ImgBB
}
