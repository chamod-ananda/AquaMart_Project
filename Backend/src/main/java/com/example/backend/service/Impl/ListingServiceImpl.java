package com.example.backend.service.Impl;

import com.example.backend.dto.ListingDTO;
import com.example.backend.entity.Listings;
import com.example.backend.repo.ListingRepository;
import com.example.backend.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {
    private final ListingRepository listingRepository;

    @Override
    public List<ListingDTO> getAllEquipments() {
        return listingRepository.findAll()
                .stream()
                .map(e -> ListingDTO.builder()
                        .id(e.getId())
                        .listingName(e.getListingName())
                        .listingDescription(e.getListingDescription())
                        .price(e.getPrice())
                        .quantity(e.getQuantity())
                        .imageUrl(e.getImageUrl())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ListingDTO addEquipment(ListingDTO dto) {
    // Ensure imageUrl is not null to satisfy DB constraint
    String imageUrl = dto.getImageUrl();
    if (imageUrl == null || imageUrl.isBlank()) {
        imageUrl = "https://via.placeholder.com/600x400?text=Listing";
    }

    Listings equipment = Listings.builder()
                .listingName(dto.getListingName())
                .listingDescription(dto.getListingDescription())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
        .imageUrl(imageUrl)
                .build();
        listingRepository.save(equipment);
        dto.setId(equipment.getId());
    dto.setImageUrl(equipment.getImageUrl());
    return dto;
    }

    @Override
    public ListingDTO updateEquipment(Long id, ListingDTO dto) {
        Listings equipment = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        equipment.setListingName(dto.getListingName());
        equipment.setListingDescription(dto.getListingDescription());
        equipment.setPrice(dto.getPrice());
        equipment.setQuantity(dto.getQuantity());
        // Preserve existing image if none provided
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            equipment.setImageUrl(dto.getImageUrl());
        }
        listingRepository.save(equipment);
        dto.setId(equipment.getId());
        dto.setImageUrl(equipment.getImageUrl());
        return dto;
    }

    @Override
    public void deleteEquipment(Long id) {
        if (!listingRepository.existsById(id)) {
            throw new RuntimeException("Equipment not found");
        }
        listingRepository.deleteById(id);
    }

    @Override
    public ListingDTO purchaseEquipment(Long id, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        Listings equipment = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        int current = equipment.getQuantity() == null ? 0 : equipment.getQuantity();
        if (current < qty) {
            throw new RuntimeException("Insufficient stock");
        }
        equipment.setQuantity(current - qty);
        listingRepository.save(equipment);
        return ListingDTO.builder()
                .id(equipment.getId())
                .listingName(equipment.getListingName())
                .listingDescription(equipment.getListingDescription())
                .price(equipment.getPrice())
                .quantity(equipment.getQuantity())
                .imageUrl(equipment.getImageUrl())
                .build();
    }
}
