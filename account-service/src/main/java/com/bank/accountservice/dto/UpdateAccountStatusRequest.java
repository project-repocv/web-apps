package com.bank.accountservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAccountStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {
}
