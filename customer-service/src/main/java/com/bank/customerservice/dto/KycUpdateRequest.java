package com.bank.customerservice.dto;

import com.bank.customerservice.entity.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating KYC status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycUpdateRequest {

    @NotNull(message = "KYC status is required")
    private KycStatus kycStatus;

    private String reviewNotes;
}
