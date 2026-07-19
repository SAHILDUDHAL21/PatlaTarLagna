package com.patlatarlagna.dto;

import com.patlatarlagna.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private Long id;
    private Long userId;
    private String content;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
}
