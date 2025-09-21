package com.example.backend.controller;

import com.example.backend.dto.ChatConversationDTO;
import com.example.backend.entity.ChatMessage;
import com.example.backend.service.ChatMessageService;
import com.example.backend.util.ImgBBService;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-chat")
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class UserChatController {

    private final ChatMessageService chatService;
    private final JWTUtil jwtUtil;
    private final ImgBBService imgBBService;

    private Long getUserId(String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid Authorization header");
        }
        Long userId = jwtUtil.extractUserId(authHeader.substring(7));
        if(userId == null) throw new SecurityException("Invalid or expired token");
        return userId;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestHeader("Authorization") String token,
                                         @RequestBody Map<String,String> body){
        try{
            Long senderId = getUserId(token);
            Long itemId = Long.valueOf(body.get("itemId"));
            Long receiverId = Long.valueOf(body.get("receiverId"));
            String content = body.getOrDefault("content", "").trim();
            if(content.isEmpty()){
                return ResponseEntity.badRequest().body(Map.of("message","Content cannot be empty"));
            }
            if(senderId.equals(receiverId)){
                return ResponseEntity.badRequest().body(Map.of("message","Cannot chat with yourself"));
            }
            ChatMessage saved = chatService.sendMessage(itemId, senderId, receiverId, content, null);
            return ResponseEntity.ok(saved);
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("message","Failed to send message: "+e.getMessage()));
        }
    }

    @GetMapping("/conversation/{itemId}/{otherUserId}")
    public ResponseEntity<?> getConversation(@RequestHeader("Authorization") String token,
                                             @PathVariable Long itemId,
                                             @PathVariable Long otherUserId){
        try{
            Long me = getUserId(token);
            List<ChatMessage> conversation = chatService.getConversation(itemId, me, otherUserId);
            return ResponseEntity.ok(conversation);
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("message","Failed to load conversation: "+e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> listConversations(@RequestHeader("Authorization") String token){
        try{
            Long me = getUserId(token);
            List<ChatConversationDTO> list = chatService.listConversations(me);
            return ResponseEntity.ok(list);
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("message","Failed to load conversations: "+e.getMessage()));
        }
    }

    @PostMapping(value="/send-image", consumes={"multipart/form-data"})
    public ResponseEntity<?> sendImage(@RequestHeader("Authorization") String token,
                                       @RequestPart("itemId") String itemIdStr,
                                       @RequestPart("receiverId") String receiverIdStr,
                                       @RequestPart(value="content", required=false) String content,
                                       @RequestPart("image") MultipartFile image){
        try {
            Long senderId = getUserId(token);
            Long itemId = Long.valueOf(itemIdStr);
            Long receiverId = Long.valueOf(receiverIdStr);
            if(senderId.equals(receiverId)){
                return ResponseEntity.badRequest().body(Map.of("message","Cannot chat with yourself"));
            }
            if(image==null || image.isEmpty()){
                return ResponseEntity.badRequest().body(Map.of("message","Image is required"));
            }
            String url = imgBBService.uploadImage(image.getBytes());
            String txt = content!=null?content.trim():null;
            if(txt!=null && txt.isBlank()) txt = null;
            // Ensure content is not null for database constraint
            if(txt == null) txt = "";
            ChatMessage saved = chatService.sendMessage(itemId, senderId, receiverId, txt, url);
            return ResponseEntity.ok(saved);
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("message","Failed to send image: "+e.getMessage()));
        }
    }
}
