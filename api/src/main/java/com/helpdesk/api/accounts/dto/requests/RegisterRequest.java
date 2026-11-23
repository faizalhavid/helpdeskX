package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Self-service sign-up. Role and group are intentionally not client-controlled. */
public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username may only contain letters, digits, '.', '_' and '-'")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100)
        String email,
        @NotBlank
        @Size(max = 72, message = "Password must be at most 72 characters")
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String password,
        @Size(max = 255) String fullName
) {
}
