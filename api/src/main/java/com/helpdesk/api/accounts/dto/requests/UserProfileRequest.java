package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record UserProfileRequest(
        @NotBlank String fullName,
        String picture
) {
}
