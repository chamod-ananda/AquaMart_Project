package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.Impl.UserServiceImpl;
import com.example.backend.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/aquamart")
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"}, allowCredentials = "true", maxAge = 3600)
@RequiredArgsConstructor
public class AuthController {
    private final UserServiceImpl userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse> registerUser(
            @RequestBody UserDTO registerDTO) {
        return ResponseEntity.ok(new APIResponse(
                200,
                "OK",
                userService.register(registerDTO)));
    }
    @PostMapping("/login")
    public ResponseEntity<APIResponse> login(
            @RequestBody AuthDTO authDTO) {
        return ResponseEntity.ok(new APIResponse(
                200,
                "OK",
                userService.authenticate(authDTO)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse> forgotPassword(
            @RequestBody ForgotPasswordRequestDTO request) {
        APIResponse response = passwordResetService.sendOtpForPasswordReset(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<APIResponse> verifyOtpAndResetPassword(
            @RequestBody VerifyOtpDTO request) {
        APIResponse response = passwordResetService.verifyOtpAndResetPassword(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
