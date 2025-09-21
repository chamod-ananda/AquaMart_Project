package com.example.backend.repo;

import com.example.backend.entity.AdoptionApplications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplications, Long> {
    List<AdoptionApplications> findByItemId(Long itemId);
    List<AdoptionApplications> findByAdopterId(Long adopterId);
    Optional<AdoptionApplications> findByAdopterIdAndItemId(Long adopterId, Long itemId);
}
