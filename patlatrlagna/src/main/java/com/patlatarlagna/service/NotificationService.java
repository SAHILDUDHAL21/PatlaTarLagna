package com.patlatarlagna.service;

import com.patlatarlagna.dto.NotificationDto;
import com.patlatarlagna.enums.NotificationType;
import java.util.List;

public interface NotificationService {
    void createNotification(Long userId, String content, NotificationType type);
    List<NotificationDto> getNotificationsForUser(Long userId);
    List<NotificationDto> getUnreadNotificationsForUser(Long userId);
    void markAsRead(Long notificationId);
}
