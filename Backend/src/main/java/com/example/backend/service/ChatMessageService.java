package com.example.backend.service;

import java.util.List;

import com.example.backend.entity.ChatMessage;

public interface ChatMessageService {
    ChatMessage sendMessage(Long itemId, Long senderId, Long receiverId, String content, String imageUrl);
    List<ChatMessage> getConversation(Long itemId, Long user1, Long user2);
    List<com.example.backend.dto.ChatConversationDTO> listConversations(Long userId);
}
