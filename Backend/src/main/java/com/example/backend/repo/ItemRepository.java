package com.example.backend.repo;

import com.example.backend.entity.Fish;
import com.example.backend.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Fish,Long> {
    List<Fish> findByOwnerId(Long ownerId);
    List<Fish> findByStatus(ItemStatus status);
}
