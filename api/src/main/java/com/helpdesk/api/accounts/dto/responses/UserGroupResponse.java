package com.helpdesk.api.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.core.models.AuditMetadataDTO;

public record UserGroupResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        String name,
        String description,
        String email
) {
}
