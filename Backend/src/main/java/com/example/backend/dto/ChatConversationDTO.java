package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationDTO {
    private Long itemId;
    private String itemName;
    private Long otherUserId;
    private String otherUsername;
    private String lastMessage;
    private Timestamp lastTimestamp;
}
