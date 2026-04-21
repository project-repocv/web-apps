package com.bank.notificationservice.dto;

import com.bank.notificationservice.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private String recipient;
    private String type;
    private String channel;
    private String content;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
}
