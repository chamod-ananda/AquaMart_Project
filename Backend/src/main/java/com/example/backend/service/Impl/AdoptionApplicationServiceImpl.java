package com.example.backend.service.Impl;

import com.example.backend.dto.AdoptionApplicationDTO;
import com.example.backend.entity.AdoptionApplications;
import com.example.backend.entity.Adoption_Status;
import com.example.backend.entity.Fish;
import com.example.backend.entity.User;
import com.example.backend.repo.AdoptionApplicationRepository;
import com.example.backend.repo.ItemRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.AdoptionApplicationService;
import com.example.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdoptionApplicationServiceImpl implements AdoptionApplicationService {
    private final AdoptionApplicationRepository adoptionRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
        private final EmailService emailService;

    @Override
    public AdoptionApplications requestAdoption(Long itemId, Long adopterId) {
        AdoptionApplications app = AdoptionApplications.builder()
                .itemId(itemId)
                .adopterId(adopterId)
                .status(Adoption_Status.PENDING)
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
        AdoptionApplications saved = adoptionRepo.save(app);

        // After saving, send an email to the fish's owner
        Fish fish = itemRepo.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Fish not found for ID: " + itemId));
        User owner = userRepo.findById(fish.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found for ID: " + fish.getOwnerId()));
        User adopter = userRepo.findById(adopterId)
                .orElseThrow(() -> new RuntimeException("Adopter not found for ID: " + adopterId));

        String subject = "New Buy Request for " + fish.getItemName();
        String body = "Hello " + owner.getUsername() + ",\n\n" +
                "You have a new buy request for your item '" + fish.getItemName() + "'.\n" +
                "Buyer: " + adopter.getUsername() + " (" + adopter.getEmail() + ")\n" +
                "Requested at: " + saved.getTimestamp() + "\n\n" +
                "Please log in to AquaMart to review and update the request status.\n\n" +
                "Thanks,\nAquaMart Team";

        try {
            emailService.sendEmail(owner.getEmail(), subject, body);
        } catch (Exception e) {
            // Log and continue; email failures shouldn't break the main flow
            System.err.println("Failed to send buy email: " + e.getMessage());
        }

        return saved;
    }

    @Override
    public List<AdoptionApplications> getAllRequests() {
        return adoptionRepo.findAll();
    }

    // Get all adoption requests for items owned by the user
    @Override
        public List<AdoptionApplicationDTO> getRequestsForMyItems(Long ownerId) {
        // First, find all items owned by this user
        List<Fish> myItems = itemRepo.findByOwnerId(ownerId);

        // Get all adoption requests for these items
        List<AdoptionApplications> requestsForMyItems = adoptionRepo.findAll()
                .stream()
                .filter(req -> myItems.stream().anyMatch(fish -> fish.getId().equals(req.getItemId())))
                .toList();

        // Map to DTOs
        return requestsForMyItems.stream()
                .map(req -> {
                    Fish fish = itemRepo.findById(req.getItemId()).orElseThrow(() ->
                            new RuntimeException("Fish not found for ID: " + req.getItemId())
                    );
                    User adopter = userRepo.findById(req.getAdopterId()).orElseThrow(() ->
                            new RuntimeException("User not found for ID: " + req.getAdopterId())
                    );
                    return AdoptionApplicationDTO.builder()
                            .id(req.getId())
                            .itemId(fish.getId())
                            .itemName(fish.getItemName())
                            .ownerUsername(adopter.getUsername())
                            .adopterName(adopter.getUsername())
                            .status(req.getStatus().name())
                            .timestamp(req.getTimestamp())
                            .build();
                }).toList();
    }

    // Update adoption request status
    @Override
    public void updateRequestStatus(Long requestId, String status) {
        AdoptionApplications app = adoptionRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Buy request not found"));
        app.setStatus(Adoption_Status.valueOf(status));
        adoptionRepo.save(app);
    }

    // Helper method to get fish name by fish ID
        @Override
        public String getItemNameById(Long itemId) {
        return itemRepo.findById(itemId)
                .map(Fish::getItemName)
                .orElse("Unknown Item");
    }

    // Helper method to get user name by user ID
        @Override
        public String getUserNameById(Long userId) {
        return userRepo.findById(userId)
                .map(User::getUsername)
                .orElse("Unknown User");
    }
}
