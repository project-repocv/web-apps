package com.bank.notificationservice.service;

import com.bank.notificationservice.dto.NotificationRequest;
import com.bank.notificationservice.dto.NotificationPreferenceRequest;
import com.bank.notificationservice.entity.Notification;
import com.bank.notificationservice.entity.NotificationPreference;

public interface NotificationService {
    Notification sendNotification(NotificationRequest request);
    void processTransactionEvent(com.bank.notificationservice.dto.TransactionEvent event);
    NotificationPreference saveNotificationPreference(NotificationPreferenceRequest request);
    NotificationPreference getNotificationPreference(String customerId);
    void scheduleNotification(Notification notification);
    void processScheduledNotifications();
}
