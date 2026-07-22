package com.patlatarlagna.service.impl;

import com.patlatarlagna.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "PatlaTarLagna - Verify Your Account";
        String body = "Welcome to PatlaTarLagna! Your email verification OTP is: " + otp + "\nExpiry: 10 minutes.";
        // Log OTP at info level so it can be seen in development when SMTP isn't available
        logger.info("Verification OTP for {} => {}", toEmail, otp);
        // Also write OTP to a local file for easy retrieval during development
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("target/otp.log");
            java.nio.file.Files.createDirectories(p.getParent());
            String line = java.time.Instant.now() + " VERIFICATION " + toEmail + " " + otp + System.lineSeparator();
            java.nio.file.Files.writeString(p, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ex) {
            logger.warn("Failed to write OTP to file: {}", ex.getMessage());
        }
        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String otp) {
        String subject = "PatlaTarLagna - Password Reset Request";
        String body = "You requested to reset your password. Use the following OTP to reset it: " + otp + "\nExpiry: 10 minutes.";
        // Log reset OTP at info level for development visibility
        logger.info("Password reset OTP for {} => {}", toEmail, otp);
        // Also write reset OTP to a local file for easy retrieval during development
        try {
            java.nio.file.Path p = java.nio.file.Paths.get("target/otp.log");
            java.nio.file.Files.createDirectories(p.getParent());
            String line = java.time.Instant.now() + " RESET " + toEmail + " " + otp + System.lineSeparator();
            java.nio.file.Files.writeString(p, line, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ex) {
            logger.warn("Failed to write OTP to file: {}", ex.getMessage());
        }
        sendEmail(toEmail, subject, body);
    }

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String body) {
        sendEmail(toEmail, "PatlaTarLagna Notification: " + subject, body);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("no-reply@patlatarlagna.com");
            mailSender.send(message);
            logger.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            logger.warn("Could not send email to {} via SMTP: {}. Fallback log: \n--- MAIL ---\nTo: {}\nSubject: {}\nBody: {}\n------------", to, e.getMessage(), to, subject, text);
        }
    }
}
