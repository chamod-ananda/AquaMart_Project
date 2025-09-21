package com.example.backend.service;

import com.example.backend.dto.APIResponse;
import com.example.backend.dto.ForgotPasswordRequestDTO;
import com.example.backend.dto.VerifyOtpDTO;

public interface PasswordResetService {
    APIResponse sendOtpForPasswordReset(ForgotPasswordRequestDTO request);
    APIResponse verifyOtpAndResetPassword(VerifyOtpDTO request);
}