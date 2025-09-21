package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private Long listingId;
    private String listingName;
    private String imageUrl;
    private Integer quantity;
    private String unitPrice;
    private String total;
    private String status;
    private LocalDateTime createdAt;
}
