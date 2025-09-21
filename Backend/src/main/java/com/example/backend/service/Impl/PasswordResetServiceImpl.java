package com.example.backend.service.Impl;

import com.example.backend.dto.APIResponse;
import com.example.backend.dto.ForgotPasswordRequestDTO;
import com.example.backend.dto.VerifyOtpDTO;
import com.example.backend.entity.PasswordResetOtp;
import com.example.backend.entity.User;
import com.example.backend.repo.PasswordResetOtpRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public APIResponse sendOtpForPasswordReset(ForgotPasswordRequestDTO request) {
        log.info("Processing password reset request for email: {}", request.getEmail());
        
        try {
            // Check if user exists
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.warn("Password reset attempted for non-existent email: {}", request.getEmail());
                // Return success even for non-existent users for security reasons
                return APIResponse.builder()
                        .status(200)
                        .message("If the email exists in our system, an OTP has been sent.")
                        .data(null)
                        .build();
            }

            // Generate 6-digit OTP
            String otp = generateOtp();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(15); // 15 minutes validity

            // Mark all previous OTPs for this email as used
            otpRepository.markAllOtpAsUsedForEmail(request.getEmail());

            // Save new OTP
            PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                    .email(request.getEmail())
                    .otp(otp)
                    .expiryTime(expiryTime)
                    .used(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            otpRepository.save(resetOtp);
            log.info("OTP generated and saved for email: {}", request.getEmail());

            // Send OTP via email
            sendOtpEmail(request.getEmail(), otp);
            log.info("OTP email sent successfully to: {}", request.getEmail());

            return APIResponse.builder()
                    .status(200)
                    .message("OTP has been sent to your email address. Please check your inbox.")
                    .data(null)
                    .build();

        } catch (Exception e) {
            log.error("Error processing password reset request for email: {}", request.getEmail(), e);
            return APIResponse.builder()
                    .status(500)
                    .message("An error occurred while processing your request. Please try again later.")
                    .data(null)
                    .build();
        }
    }

    @Override
    @Transactional
    public APIResponse verifyOtpAndResetPassword(VerifyOtpDTO request) {
        log.info("Processing OTP verification for email: {}", request.getEmail());

        try {
            // Clean up expired OTPs first
            otpRepository.deleteExpiredOtps(LocalDateTime.now());

            // Find valid OTP
            Optional<PasswordResetOtp> otpOptional = otpRepository.findByEmailAndOtpAndUsedFalseAndExpiryTimeAfter(
                    request.getEmail(), 
                    request.getOtp(), 
                    LocalDateTime.now()
            );

            if (otpOptional.isEmpty()) {
                log.warn("Invalid or expired OTP attempted for email: {}", request.getEmail());
                return APIResponse.builder()
                        .status(400)
                        .message("Invalid or expired OTP. Please request a new one.")
                        .data(null)
                        .build();
            }

            // Find user
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isEmpty()) {
                log.error("User not found during OTP verification for email: {}", request.getEmail());
                return APIResponse.builder()
                        .status(404)
                        .message("User not found.")
                        .data(null)
                        .build();
            }

            // Validate new password
            if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 6) {
                return APIResponse.builder()
                        .status(400)
                        .message("New password must be at least 6 characters long.")
                        .data(null)
                        .build();
            }

            // Update user password
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Mark OTP as used
            PasswordResetOtp otp = otpOptional.get();
            otp.setUsed(true);
            otpRepository.save(otp);

            log.info("Password reset successfully completed for email: {}", request.getEmail());

            return APIResponse.builder()
                    .status(200)
                    .message("Password has been reset successfully. You can now login with your new password.")
                    .data(null)
                    .build();

        } catch (Exception e) {
            log.error("Error during OTP verification for email: {}", request.getEmail(), e);
            return APIResponse.builder()
                    .status(500)
                    .message("An error occurred while resetting your password. Please try again.")
                    .data(null)
                    .build();
        }
    }

    private String generateOtp() {
        // Generate 6-digit OTP
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    private void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("infaquamart@gmail.com");
        helper.setTo(toEmail);
    helper.setSubject("� AquaMart Password Reset - OTP Verification");

        String body = buildOtpEmailBody(otp);
        helper.setText(body, true);

        mailSender.send(message);
    }

    private String buildOtpEmailBody(String otp) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;\">" +
               "  <div style=\"background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;\">" +
               "    <h1 style=\"color: white; margin: 0; font-size: 28px;\">� AquaMart</h1>" +
               "    <p style=\"color: #f8f9fa; margin: 10px 0 0 0; font-size: 16px;\">Password Reset Request</p>" +
               "  </div>" +
               "  <div style=\"background: white; padding: 40px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">" +
               "    <h2 style=\"color: #333; margin-bottom: 20px; text-align: center;\">Verify Your Identity</h2>" +
               "    <p style=\"color: #666; font-size: 16px; line-height: 1.5; margin-bottom: 30px; text-align: center;\">" +
               "      We received a request to reset your password. Please use the following One-Time Password (OTP) to proceed:" +
               "    </p>" +
               "    <div style=\"background: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 30px; text-align: center; margin: 30px 0;\">" +
               "      <div style=\"font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin-bottom: 10px;\">" + otp + "</div>" +
               "      <p style=\"color: #666; margin: 0; font-size: 14px;\">This OTP is valid for 15 minutes</p>" +
               "    </div>" +
               "    <div style=\"background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0;\">" +
               "      <p style=\"margin: 0; color: #856404; font-size: 14px;\">⚠️ <strong>Important:</strong> If you didn't request this password reset, please ignore this email and consider changing your password if you suspect unauthorized access.</p>" +
               "    </div>" +
               "    <div style=\"text-align: center; margin-top: 30px;\">" +
               "      <p style=\"color: #666; margin: 0; font-size: 14px;\">Need help? Contact our support team at infaquamart@gmail.com</p>" +
               "    </div>" +
               "  </div>" +
               "  <div style=\"text-align: center; padding: 20px; color: #666; font-size: 12px;\">" +
               "    <p>© 2025 AquaMart. All rights reserved.</p>" +
               "  </div>" +
               "</div>";
    }
}