package com.helpdesk.api.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.core.models.AuditMetadataDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserProfileResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        @NotBlank String fullName,
        String picture,
        @NotNull UUID userId
) {
}
