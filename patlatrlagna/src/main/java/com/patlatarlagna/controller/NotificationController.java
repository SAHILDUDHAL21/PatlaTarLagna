package com.patlatarlagna.controller;

import com.patlatarlagna.dto.ApiResponse;
import com.patlatarlagna.dto.NotificationDto;
import com.patlatarlagna.security.CustomUserDetails;
import com.patlatarlagna.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationDto> notifications = notificationService.getNotificationsForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsForUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Unread notifications retrieved successfully", notifications));
    }

    @PostMapping("/read/{notificationId}")
    public ResponseEntity<ApiResponse<String>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }
}
