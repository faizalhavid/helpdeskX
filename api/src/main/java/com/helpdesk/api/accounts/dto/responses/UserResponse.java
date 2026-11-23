package com.helpdesk.api.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.core.models.AuditMetadataDTO;

import java.util.UUID;

public record UserResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        String username,
        String email,
        UUID roleId,
        String role,
        UUID userGroupId,
        UserProfileResponse profile
) {
}
