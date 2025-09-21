package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequestDTO {
    @Email(message = "Invalid email format")
    private String email;
}