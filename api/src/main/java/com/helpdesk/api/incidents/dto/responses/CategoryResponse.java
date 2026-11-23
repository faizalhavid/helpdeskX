package com.helpdesk.api.incidents.dto.responses;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.helpdesk.api.core.models.AuditMetadataDTO;

public record CategoryResponse (
        @JsonUnwrapped
        AuditMetadataDTO metadata,
        String name,
        String description
){ }
