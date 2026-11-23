package com.helpdesk.api.accounts.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.accounts.entities.enums.ActionType;
import com.helpdesk.api.core.models.AuditMetadataDTO;

/**
 * @param authority the string checked by {@code hasAuthority(...)}, e.g. {@code USER:READ}
 */
public record PrivilegeResponse(
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        String resource,
        ActionType action,
        String authority,
        String description
) {
}
