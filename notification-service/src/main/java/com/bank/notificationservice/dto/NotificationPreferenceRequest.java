package com.bank.notificationservice.dto;

import com.bank.notificationservice.entity.NotificationChannel;
import com.bank.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferenceRequest {
    private String customerId;
    private Set<NotificationChannel> channels;
    private Set<NotificationType> types;
    private Boolean enabled;
}
