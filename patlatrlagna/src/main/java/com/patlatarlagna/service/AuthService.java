package com.patlatarlagna.service;

import com.patlatarlagna.dto.*;

public interface AuthService {
    JwtResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    void verifyEmail(String email, String otp);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    void changePassword(String email, ChangePasswordRequest changePasswordRequest);
    JwtResponse refreshJwtToken(TokenRefreshRequest refreshRequest);
}
