package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "adoptionApplications")
public class AdoptionApplications {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long adopterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Adoption_Status status; //pending, approved, rejected

    @Column(nullable = false)
    private Timestamp timestamp;
}
