package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId; // Conversation scoped to a fish

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String content; // optional text

    @Column(nullable = true)
    private String imageUrl; // optional image

    @Column(nullable = false)
    private Timestamp timestamp;

    @PrePersist
    public void prePersist(){
        if(timestamp == null){
            timestamp = new Timestamp(System.currentTimeMillis());
        }
    }
}
