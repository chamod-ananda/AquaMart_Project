package com.example.backend.repo;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRole(Role role);
    boolean existsByUsername(String username);
    List<User> findByRole(Role role);
    long countByRole(Role role);
}
