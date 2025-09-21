package com.example.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {
    private String email;
    private String password;
}
