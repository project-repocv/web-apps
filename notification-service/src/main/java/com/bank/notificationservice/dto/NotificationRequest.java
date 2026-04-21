package com.bank.notificationservice.dto;

import com.bank.notificationservice.entity.NotificationChannel;
import com.bank.notificationservice.entity.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @NotNull(message = "Notification channel is required")
    private NotificationChannel channel;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String transactionId;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String phoneNumber;
}
