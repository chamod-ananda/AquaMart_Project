package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListingDTO {
    private Long id;
    @NotBlank(message = "Listing Name is required")
    private String listingName;
    @NotBlank(message = "Listing Description is required")
    private String listingDescription;
    @NotBlank(message = "Price is required")
    private String price;
    @NotBlank(message = "Quantity is required")
    private Integer quantity;
    @NotBlank(message = "Image is required")
    private String imageUrl;
}
