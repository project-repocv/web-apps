package com.bank.customerservice.entity;

/**
 * KYC (Know Your Customer) status enumeration.
 */
public enum KycStatus {
    PENDING,      // KYC verification pending
    IN_PROGRESS,  // KYC verification in progress
    VERIFIED,     // KYC successfully verified
    REJECTED,     // KYC verification rejected
    EXPIRED       // KYC has expired and needs renewal
}
