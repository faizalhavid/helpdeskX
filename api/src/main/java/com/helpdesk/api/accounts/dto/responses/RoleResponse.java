package com.helpdesk.api.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.core.models.AuditMetadataDTO;

import java.util.List;

public record RoleResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        String role,
        String description,
        List<PrivilegeResponse> privileges
) {
}
