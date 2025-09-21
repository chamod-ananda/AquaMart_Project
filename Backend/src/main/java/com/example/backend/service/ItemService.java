package com.example.backend.service;

import java.util.List;

import com.example.backend.dto.ItemDTO;

public interface ItemService {
    ItemDTO createItem(ItemDTO itemDTO, Long ownerId, byte[] imageBytes);

    // Get all items (admin or general)
    List<ItemDTO> getAllItems();

    // Get items by ownerId (for My Items page)
    List<ItemDTO> getItemsByOwnerId(Long ownerId);

    // Update fish by id (must match ownerId)
    ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId, byte[] imageBytes);

    // Delete fish by id (must match ownerId)
    void deleteItem(Long itemId, Long ownerId);

}