package com.example.backend.service.Impl;

import com.example.backend.dto.ItemDTO;
import com.example.backend.entity.Fish;
import com.example.backend.entity.ItemStatus;
import com.example.backend.repo.ItemRepository;
import com.example.backend.service.ItemService;
import com.example.backend.util.ImgBBService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ImgBBService imgBBService;

    private ItemDTO toDTO(Fish fish) {
        return ItemDTO.builder()
                .id(fish.getId())
                .itemName(fish.getItemName())
                .status(fish.getStatus().name())
                .ownerId(fish.getOwnerId())
                .imageUrl(fish.getImageUrl())
                .build();
    }

    private Fish toEntity(ItemDTO dto, Long ownerId, String imageUrl) {
        return Fish.builder()
                .itemName(dto.getItemName())
                .status(ItemStatus.valueOf(dto.getStatus()))
                .ownerId(ownerId)
                .imageUrl(imageUrl)
                .build();
    }

    @Override
    public List<ItemDTO> getAllItems() {
        return itemRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ItemDTO createItem(ItemDTO itemDTO, Long ownerId, byte[] imageBytes) {
        String uploadedUrl = imgBBService.uploadImage(imageBytes);
        Fish fish = toEntity(itemDTO, ownerId, uploadedUrl);
        Fish saved = itemRepository.save(fish);
        return toDTO(saved);
    }

    @Override
    public List<ItemDTO> getItemsByOwnerId(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public ItemDTO updateItem(Long itemId, ItemDTO itemDTO, Long ownerId, byte[] imageBytes) {
        Fish existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Fish not found"));

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }

        String newImageUrl = existingItem.getImageUrl();
        if (imageBytes != null && imageBytes.length > 0) {
            newImageUrl = imgBBService.uploadImage(imageBytes);
        }

        existingItem.setItemName(itemDTO.getItemName());
        existingItem.setStatus(ItemStatus.valueOf(itemDTO.getStatus()));
        existingItem.setImageUrl(newImageUrl);

        Fish updated = itemRepository.save(existingItem);
        return toDTO(updated);
    }

    @Override
    public void deleteItem(Long itemId, Long ownerId) {
        Fish existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Fish not found"));

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Unauthorized");
        }
        itemRepository.deleteById(itemId);
    }
}
