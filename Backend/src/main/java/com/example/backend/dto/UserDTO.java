package com.example.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    @NotBlank(message = "Username is required")
    private String username;
    @Email(message = "Invalid email format")
    private String email;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
