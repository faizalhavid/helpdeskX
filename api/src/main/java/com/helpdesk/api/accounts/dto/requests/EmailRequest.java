package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Used by forgot-password and resend-verification. */
public record EmailRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {
}
