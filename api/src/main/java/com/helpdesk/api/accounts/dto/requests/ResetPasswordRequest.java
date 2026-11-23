package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 200) String token,
        @NotBlank
        @Size(max = 72, message = "Password must be at most 72 characters")
        @Pattern(regexp = PasswordPolicy.REGEX, message = PasswordPolicy.MESSAGE)
        String newPassword
) {
}
