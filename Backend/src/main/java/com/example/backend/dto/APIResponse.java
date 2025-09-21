package com.example.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {
    private int status;
    private String message;
    private Object data;
}
