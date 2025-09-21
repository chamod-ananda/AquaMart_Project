package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDTO {
    private Long id;
    @NotBlank(message = "Item name is required")
    private String itemName;
    @NotBlank(message = "Status is required")
    private String status; // AVAILABLE or NOT_AVAILABLE
    @NotNull(message = "Owner ID is required")
    private Long ownerId;
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
}