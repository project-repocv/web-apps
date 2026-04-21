package com.bank.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SmsService {
    
    @Async
    public CompletableFuture<Void> sendSms(String phoneNumber, String message) {
        try {
            Thread.sleep(100);
            log.info("Sending SMS to: {}, Message: {}", phoneNumber, message);
            return CompletableFuture.completedFuture(null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("SMS sending interrupted for: {}", phoneNumber, e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            return CompletableFuture.failedFuture(e);
        }
    }
    
    public boolean validatePhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("\\+?[1-9]\\d{1,14}");
    }
}
