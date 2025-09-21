package com.example.backend.controller;

import com.example.backend.dto.AdoptionApplicationDTO;
import com.example.backend.entity.AdoptionApplications;
import com.example.backend.entity.Adoption_Status;
import com.example.backend.entity.Fish;
import com.example.backend.repo.AdoptionApplicationRepository;
import com.example.backend.repo.ItemRepository;
import com.example.backend.service.AdoptionApplicationService;
import com.example.backend.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/adoption")
@CrossOrigin(origins = "http://localhost:5500") // adjust to your frontend origin
@RequiredArgsConstructor
@Slf4j
public class AdoptionApplicationController {

    private final AdoptionApplicationService adoptionService;
    private final AdoptionApplicationRepository adoptionRepo;
    private final ItemRepository itemRepository;
    private final JWTUtil jwtUtil;

    @PostMapping("/request/{itemId}")
    public ResponseEntity<?> requestAdoption(@PathVariable Long itemId,
                                             @RequestBody Map<String,String> body,
                                             @RequestHeader("Authorization") String token) {
        try {
            Long adopterId = getUserIdFromToken(token);

            // Check if the fish exists
            Fish fish = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Fish not found"));

            // Prevent self-adoption: user cannot adopt their own pet
            if (fish.getOwnerId().equals(adopterId)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "You cannot adopt your own pet!");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if user has already requested adoption for this fish
            boolean alreadyRequested = adoptionRepo.findByAdopterIdAndItemId(adopterId, itemId).isPresent();
            if (alreadyRequested) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "You have already submitted an adoption request for this fish!");
                return ResponseEntity.badRequest().body(error);
            }

            AdoptionApplications app = adoptionService.requestAdoption(itemId, adopterId);
            return ResponseEntity.ok(app);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to submit adoption request: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdoptionApplicationDTO>> getAllAdoptions() {
        List<AdoptionApplications> apps = adoptionService.getAllRequests();
        List<AdoptionApplicationDTO> dtos = apps.stream()
                .map(app -> {
                    // Get fish name and adopter name using the service helper methods
                    String itemName = adoptionService.getItemNameById(app.getItemId());
                    String adopterName = adoptionService.getUserNameById(app.getAdopterId());

                    return AdoptionApplicationDTO.builder()
                            .id(app.getId())
                            .itemId(app.getItemId())
                            .itemName(itemName)
                            .ownerUsername(adopterName)
                            .status(app.getStatus().name())
                            .timestamp(app.getTimestamp())
                            .build();
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping({"/my-items", "/my-items"})
    public ResponseEntity<?> getMyItemsAdoptionRequests(@RequestHeader("Authorization") String token) {
        try {
            Long ownerId = getUserIdFromToken(token);
            List<AdoptionApplicationDTO> requests = adoptionService.getRequestsForMyItems(ownerId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            // Log the error for debugging
            log.error("Failed to fetch adoption requests", e);

            // Return a proper error message in the response
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to fetch adoption requests: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyAdoptionRequests(@RequestHeader("Authorization") String token) {
        try {
            Long adopterId = getUserIdFromToken(token);
            List<AdoptionApplications> requests = adoptionRepo.findByAdopterId(adopterId);

            List<Map<String, Object>> response = requests.stream().map(r -> {
                Map<String, Object> map = new HashMap<>();
                Fish fish = itemRepository.findById(r.getItemId()).orElse(null);
                map.put("itemName", fish != null ? fish.getItemName() : "Unknown");
                if(fish != null){
                    // use helper service to get owner username
                    String ownerUsername = adoptionService.getUserNameById(fish.getOwnerId());
                    map.put("ownerUsername", ownerUsername);
                } else {
                    map.put("ownerUsername", "Unknown");
                }
                map.put("status", r.getStatus());
                map.put("requestId", r.getId());
                map.put("timestamp", r.getTimestamp());
                return map;
            }).toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving requests: " + e.getMessage());
        }
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateAdoptionStatus(@PathVariable Long id,
                                                  @RequestParam String status,
                                                  @RequestHeader("Authorization") String token) {
        try {
            Long ownerId = getUserIdFromToken(token);
            AdoptionApplications app = adoptionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            Fish fish = itemRepository.findById(app.getItemId())
                    .orElseThrow(() -> new RuntimeException("Fish not found"));

            if (!fish.getOwnerId().equals(ownerId)) {
                return ResponseEntity.status(403).body("Unauthorized");
            }

            app.setStatus(Adoption_Status.valueOf(status.toUpperCase()));
            adoptionRepo.save(app);
            return ResponseEntity.ok(app);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating status: " + e.getMessage());
        }
    }

    // Helper method to extract userId from JWT
    private Long getUserIdFromToken(String authHeader){
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
}
