package com.bank.notificationservice.service;

import com.bank.notificationservice.dto.NotificationRequest;
import com.bank.notificationservice.dto.NotificationPreferenceRequest;
import com.bank.notificationservice.dto.TransactionEvent;
import com.bank.notificationservice.entity.*;
import com.bank.notificationservice.repository.NotificationPreferenceRepository;
import com.bank.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    
    @Override
    public Notification sendNotification(NotificationRequest request) {
        NotificationPreference preference = preferenceRepository.findByCustomerId(request.getRecipient())
                .orElse(NotificationPreference.builder()
                        .customerId(request.getRecipient())
                        .channels(Set.of(NotificationChannel.EMAIL))
                        .types(Set.of(request.getType()))
                        .enabled(true)
                        .build());
        
        if (!preference.isEnabled() || !preference.getTypes().contains(request.getType())) {
            log.info("Notification disabled for customer: {} and type: {}", 
                    request.getRecipient(), request.getType());
            return null;
        }
        
        Notification notification = Notification.builder()
                .recipient(request.getRecipient())
                .type(request.getType())
                .channel(request.getChannel())
                .content(request.getContent())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .transactionId(request.getTransactionId())
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        deliverNotification(savedNotification);
        
        return savedNotification;
    }
    
    @Override
    public void processTransactionEvent(TransactionEvent event) {
        log.info("Processing transaction event: {}", event.getTransactionId());
        
        NotificationType notificationType = determineNotificationType(event);
        String content = buildTransactionNotificationContent(event);
        
        NotificationRequest request = NotificationRequest.builder()
                .recipient(event.getCustomerId())
                .type(notificationType)
                .channel(NotificationChannel.EMAIL)
                .content(content)
                .transactionId(event.getTransactionId())
                .build();
        
        sendNotification(request);
        
        if (isCriticalTransaction(event)) {
            NotificationRequest smsRequest = NotificationRequest.builder()
                    .recipient(event.getCustomerId())
                    .type(notificationType)
                    .channel(NotificationChannel.SMS)
                    .content(content)
                    .transactionId(event.getTransactionId())
                    .build();
            
            sendNotification(smsRequest);
        }
    }
    
    @Override
    public NotificationPreference saveNotificationPreference(NotificationPreferenceRequest request) {
        NotificationPreference existing = preferenceRepository.findByCustomerId(request.getCustomerId())
                .orElse(null);
        
        if (existing != null) {
            existing.setChannels(request.getChannels());
            existing.setTypes(request.getTypes());
            existing.setEnabled(request.getEnabled());
            return preferenceRepository.save(existing);
        } else {
            NotificationPreference newPreference = NotificationPreference.builder()
                    .customerId(request.getCustomerId())
                    .channels(request.getChannels())
                    .types(request.getTypes())
                    .enabled(request.getEnabled())
                    .build();
            return preferenceRepository.save(newPreference);
        }
    }
    
    @Override
    public NotificationPreference getNotificationPreference(String customerId) {
        return preferenceRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Notification preference not found for customer: " + customerId));
    }
    
    @Override
    public void scheduleNotification(Notification notification) {
        notification.setStatus(NotificationStatus.PENDING);
        notificationRepository.save(notification);
    }
    
    @Override
    public void processScheduledNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findByStatusAndRetryCountLessThan(
                NotificationStatus.PENDING, 3);
        
        for (Notification notification : pendingNotifications) {
            deliverNotification(notification);
        }
    }
    
    private void deliverNotification(Notification notification) {
        try {
            switch (notification.getChannel()) {
                case EMAIL:
                    emailService.sendEmail(notification.getRecipient(), 
                                         "Banking Alert: " + notification.getType(), 
                                         notification.getContent());
                    break;
                case SMS:
                    smsService.sendSms(notification.getRecipient(), notification.getContent());
                    break;
                case PUSH_NOTIFICATION:
                    log.info("Push notification not implemented yet");
                    break;
            }
            
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            
            log.info("Notification sent successfully: {}", notification.getId());
        } catch (Exception e) {
            log.error("Failed to send notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
            notificationRepository.save(notification);
        }
    }
    
    private NotificationType determineNotificationType(TransactionEvent event) {
        return NotificationType.TRANSACTION_ALERT;
    }
    
    private String buildTransactionNotificationContent(TransactionEvent event) {
        StringBuilder content = new StringBuilder();
        content.append("Transaction Alert\n");
        content.append("ID: ").append(event.getTransactionId()).append("\n");
        content.append("Type: ").append(event.getType()).append("\n");
        content.append("Amount: ").append(event.getAmount()).append(" ").append(event.getCurrency()).append("\n");
        content.append("Time: ").append(event.getTimestamp()).append("\n");
        content.append("Description: ").append(event.getDescription()).append("\n");
        content.append("Status: ").append(event.getStatus()).append("\n");
        return content.toString();
    }
    
    private boolean isCriticalTransaction(TransactionEvent event) {
        return event.getAmount().compareTo(java.math.BigDecimal.valueOf(1000)) > 0;
    }
}
