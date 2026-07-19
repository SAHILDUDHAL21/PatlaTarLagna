package com.patlatarlagna.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
    void sendPasswordResetEmail(String toEmail, String otp);
    void sendNotificationEmail(String toEmail, String subject, String body);
}
