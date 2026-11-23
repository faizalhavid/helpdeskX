package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserGroupRequest(
        @NotBlank String name,
        String description,
        @Email String email
) {
}
