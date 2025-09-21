package com.example.backend.repo;

import com.example.backend.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    
    Optional<PasswordResetOtp> findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
        String email, String otp, LocalDateTime currentTime);
    
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetOtp p SET p.used = true WHERE p.email = :email AND p.used = false")
    void markAllOtpAsUsedForEmail(@Param("email") String email);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOtp p WHERE p.expiryTime < :currentTime")
    void deleteExpiredOtps(@Param("currentTime") LocalDateTime currentTime);
}