package com.example.backend.repo;

import com.example.backend.entity.Listings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<Listings, Long> {
}
