package com.example.backend.service;

import com.example.backend.dto.AdoptionApplicationDTO;
import com.example.backend.entity.AdoptionApplications;

import java.util.List;

public interface AdoptionApplicationService {
    AdoptionApplications requestAdoption(Long itemId, Long adopterId);
    List<AdoptionApplications> getAllRequests();
    List<AdoptionApplicationDTO> getRequestsForMyItems(Long ownerId);
    void updateRequestStatus(Long requestId, String status);

    // Helper methods to get names by ID
    String getItemNameById(Long itemId);
    String getUserNameById(Long userId);

}
