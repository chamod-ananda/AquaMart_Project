package com.example.backend.controller;

import com.example.backend.dto.ItemDTO;
import com.example.backend.service.ItemService;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "http://localhost:5500")
@RequiredArgsConstructor
public class FishController {

    private final ItemService itemService;
    private final JWTUtil jwtUtil;

    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new SecurityException("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            throw new SecurityException("Invalid or expired token");
        }
        return userId;
    }

    @PostMapping(
            value = "/saveItem",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ItemDTO> addItem(@RequestPart("fish") ItemDTO itemDTO,
                                          @RequestPart("image") MultipartFile image,
                                          @RequestHeader("Authorization") String authHeader) throws Exception {
        Long ownerId = getUserIdFromToken(authHeader);
        byte[] imageBytes = image.getBytes();
        ItemDTO createdItem = itemService.createItem(itemDTO, ownerId, imageBytes);
        return ResponseEntity.ok(createdItem);
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getMyItems(@RequestHeader("Authorization") String authHeader) {
        Long ownerId = getUserIdFromToken(authHeader);
        return ResponseEntity.ok(itemService.getItemsByOwnerId(ownerId));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ItemDTO> updateItem(@PathVariable Long id,
                                             @RequestPart("fish") ItemDTO itemDTO,
                                             @RequestPart(value = "image", required = false) MultipartFile image,
                                             @RequestHeader("Authorization") String authHeader) throws Exception {
        Long ownerId = getUserIdFromToken(authHeader);
        byte[] imageBytes = image != null ? image.getBytes() : null;
        return ResponseEntity.ok(itemService.updateItem(id, itemDTO, ownerId, imageBytes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id,
                                          @RequestHeader("Authorization") String authHeader) {
        Long ownerId = getUserIdFromToken(authHeader);
        itemService.deleteItem(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }
}
