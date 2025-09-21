package com.example.backend.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdoptionApplicationDTO {
    private Long id;
    private Long itemId;
    private String itemName;  // Added fish name field
    private String ownerUsername; // <-- add this
    private String adopterName;  // Added adopter name field
    private String status;
    private Timestamp timestamp;
}
