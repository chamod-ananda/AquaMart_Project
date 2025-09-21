package com.example.backend.service.Impl;

import com.example.backend.dto.ChatConversationDTO;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.Fish;
import com.example.backend.entity.User;
import com.example.backend.repo.ChatMessageRepository;
import com.example.backend.repo.ItemRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;

    @Override
    public ChatMessage sendMessage(Long itemId, Long senderId, Long receiverId, String content, String imageUrl) {
        ChatMessage msg = ChatMessage.builder()
                .itemId(itemId)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .imageUrl(imageUrl)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        return chatRepo.save(msg);
    }

    @Override
    public List<ChatMessage> getConversation(Long itemId, Long user1, Long user2) {
        return chatRepo.findConversation(itemId, user1, user2);
    }

    @Override
    public List<ChatConversationDTO> listConversations(Long userId) {
        // Fetch all messages involving user ordered desc by timestamp
        List<ChatMessage> messages = chatRepo.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);
        java.util.Map<String, ChatConversationDTO> map = new java.util.LinkedHashMap<>();
        for(ChatMessage m : messages){
            Long other = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            
            // Skip self-conversations
            if(other.equals(userId)) {
                continue;
            }
            
            String key = m.getItemId()+":"+other;
            if(!map.containsKey(key)){
                Fish fish = itemRepo.findById(m.getItemId()).orElse(null);
                User otherUser = userRepo.findById(other).orElse(null);
        String preview = (m.getContent()==null || m.getContent().isBlank()) && m.getImageUrl()!=null ? "[Image]" : m.getContent();
    map.put(key, ChatConversationDTO.builder()
                        .itemId(m.getItemId())
            .itemName(fish!=null?fish.getItemName():"Unknown Fish")
                        .otherUserId(other)
                        .otherUsername(otherUser!=null?otherUser.getUsername():"Unknown User")
            .lastMessage(preview)
                        .lastTimestamp(m.getTimestamp())
                        .build());
            }
        }
        return new java.util.ArrayList<>(map.values());
    }
}
