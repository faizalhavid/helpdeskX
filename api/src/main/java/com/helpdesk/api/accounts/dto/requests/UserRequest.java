package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UserRequest(
        @NotBlank String username,
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        String password,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Role ID is required")
        String roleId,
        String userGroupId
) {
    public UserRequest(String username, String password, String email) {
        this(username, password, email, null, null);
    }
}
