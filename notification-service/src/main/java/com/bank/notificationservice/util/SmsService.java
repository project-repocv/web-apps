package com.bank.notificationservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {
    
    public void sendSms(String phoneNumber, String message) {
        try {
            // Mock SMS implementation - in production, integrate with Twilio, AWS SNS, etc.
            log.info("SMS sent to {}: {}", phoneNumber, message);
            
            // Simulate SMS sending delay
            Thread.sleep(100);
            
            log.info("SMS successfully sent to: {}", phoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}
