package com.patlatarlagna.dto;

import com.patlatarlagna.enums.InterestStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderPhoto;
    private Long receiverId;
    private String receiverName;
    private String receiverPhoto;
    private InterestStatus status;
    private LocalDateTime createdAt;
}
