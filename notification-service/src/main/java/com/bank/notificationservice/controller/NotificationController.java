package com.bank.notificationservice.controller;

import com.bank.notificationservice.dto.*;
import com.bank.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse> sendNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Send notification request received for recipient: {}", request.getRecipient());
        
        try {
            Notification notification = notificationService.sendNotification(request);
            
            if (notification != null) {
                ApiResponse response = ApiResponse.builder()
                        .success(true)
                        .message("Notification sent successfully")
                        .data(notification.getId())
                        .build();
                return ResponseEntity.ok(response);
            } else {
                ApiResponse response = ApiResponse.builder()
                        .success(false)
                        .message("Notification was not sent due to preferences")
                        .build();
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error sending notification for recipient: {}", request.getRecipient(), e);
            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to send notification: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/preferences")
    public ResponseEntity<ApiResponse> saveNotificationPreference(@Valid @RequestBody NotificationPreferenceRequest request) {
        log.info("Save notification preference request received for customer: {}", request.getCustomerId());
        
        try {
            var preference = notificationService.saveNotificationPreference(request);
            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Notification preference saved successfully")
                    .data(preference)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving notification preference", e);
            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to save notification preference: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/preferences/{customerId}")
    public ResponseEntity<ApiResponse> getNotificationPreference(@PathVariable String customerId) {
        log.info("Get notification preference request for customer: {}", customerId);
        
        try {
            var preference = notificationService.getNotificationPreference(customerId);
            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Notification preference retrieved successfully")
                    .data(preference)
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting notification preference", e);
            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to retrieve notification preference: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/process-pending")
    public ResponseEntity<ApiResponse> processPendingNotifications() {
        log.info("Process pending notifications request received");
        
        try {
            notificationService.processScheduledNotifications();
            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Pending notifications processed successfully")
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing pending notifications", e);
            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Failed to process pending notifications: " + e.getMessage())
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
