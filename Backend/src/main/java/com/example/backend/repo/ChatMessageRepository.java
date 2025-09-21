package com.example.backend.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.itemId = :itemId AND ((m.senderId = :u1 AND m.receiverId = :u2) OR (m.senderId = :u2 AND m.receiverId = :u1)) ORDER BY m.timestamp ASC")
    List<ChatMessage> findConversation(@Param("itemId") Long itemId,
                                       @Param("u1") Long user1,
                                       @Param("u2") Long user2);

    List<ChatMessage> findBySenderIdOrReceiverIdOrderByTimestampDesc(Long senderId, Long receiverId);
}
