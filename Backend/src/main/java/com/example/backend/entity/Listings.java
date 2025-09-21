package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "listings")
public class Listings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String listingName;

    @Column(nullable = false)
    private String listingDescription;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String imageUrl;  // saved after uploading to ImgBB
}
