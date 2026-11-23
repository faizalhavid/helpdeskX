package com.helpdesk.api.accounts.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRequest(
        @NotBlank @Size(max = 200) String token
) {
}
